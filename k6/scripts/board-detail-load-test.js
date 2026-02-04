import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';
import { SharedArray } from 'k6/data';

/**
 * Board Detail Load Test
 *
 * Tests: GET /community/board/detail/{code}
 * Pattern: Ramping VUs (0 -> 30 -> 60 -> 0)
 * Threshold: p95 < 1000ms
 *
 * Usage:
 *   k6 run k6/scripts/board-detail-load-test.js
 *   k6 run --env BASE_URL=http://localhost:8080 k6/scripts/board-detail-load-test.js
 */

// Configuration
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const API_PREFIX = __ENV.API_PREFIX || '/api';

// Custom metrics
const boardDetailRequests = new Counter('board_detail_requests');
const boardDetailErrors = new Rate('board_detail_errors');
const boardDetailDuration = new Trend('board_detail_duration');

export const options = {
    scenarios: {
        board_detail_load: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '30s', target: 30 },   // Ramp up to 30 VUs
                { duration: '1m', target: 60 },    // Stay at 60 VUs
                { duration: '2m', target: 60 },    // Continue at 60 VUs
                { duration: '30s', target: 30 },   // Ramp down to 30
                { duration: '30s', target: 0 },    // Ramp down to 0
            ],
            gracefulRampDown: '10s',
        },
    },
    thresholds: {
        'http_req_duration': ['p(95)<1000', 'p(99)<2000'],
        'http_req_failed': ['rate<0.05'],
        'board_detail_errors': ['rate<0.05'],
        'board_detail_duration': ['p(95)<1000'],
    },
};

// Sample board codes - in real test, these should be loaded from actual data
let boardCodes = [];

export function setup() {
    console.log(`Testing against: ${BASE_URL}`);

    // Fetch some board codes from the list API
    const listResponse = http.get(`${BASE_URL}${API_PREFIX}/boards?pageNum=1&pageSize=100`, {
        timeout: '10s',
    });

    if (listResponse.status === 200) {
        try {
            const data = JSON.parse(listResponse.body);
            if (data.data && data.data.content) {
                boardCodes = data.data.content.map(board => board.code);
                console.log(`Loaded ${boardCodes.length} board codes for testing`);
            }
        } catch (e) {
            console.error('Failed to parse board list response');
        }
    }

    // Fallback board codes if API doesn't return any
    if (boardCodes.length === 0) {
        console.warn('Using fallback board codes');
        boardCodes = [
            'BO_1', 'BO_2', 'BO_3', 'BO_4', 'BO_5',
            'BO_6', 'BO_7', 'BO_8', 'BO_9', 'BO_10'
        ];
    }

    return {
        boardCodes: boardCodes,
    };
}

export default function (data) {
    // Select a random board code
    const boardCode = data.boardCodes[Math.floor(Math.random() * data.boardCodes.length)];

    // Build request URL
    const url = `${BASE_URL}${API_PREFIX}/boards/${boardCode}`;

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
        },
        tags: { name: 'BoardDetail' },
    };

    const startTime = Date.now();
    const response = http.get(url, params);
    const duration = Date.now() - startTime;

    // Track metrics
    boardDetailRequests.add(1);
    boardDetailDuration.add(duration);

    // Validate response
    const success = check(response, {
        'status is 200 or 404': (r) => r.status === 200 || r.status === 404,
        'response time < 1000ms': (r) => r.timings.duration < 1000,
        'response has content': (r) => r.body && r.body.length > 0,
        'response is JSON': (r) => {
            try {
                JSON.parse(r.body);
                return true;
            } catch (e) {
                return false;
            }
        },
        'has board data': (r) => {
            if (r.status === 404) return true;  // 404 is acceptable
            try {
                const json = JSON.parse(r.body);
                return json.data && json.data.code;
            } catch (e) {
                return false;
            }
        },
    });

    if (!success) {
        boardDetailErrors.add(1);
        if (response.status !== 404) {
            console.error(`Request failed: ${response.status} - ${response.body}`);
        }
    } else {
        boardDetailErrors.add(0);
    }

    // Think time - simulate user reading the post
    sleep(Math.random() * 5 + 2);  // 2-7 seconds
}

export function teardown(data) {
    console.log('Board Detail Load Test completed');
}

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

/**
 * Board List Load Test
 *
 * Tests: GET /community/board/list (or /api/boards)
 * Pattern: Ramping VUs (0 -> 50 -> 100 -> 0)
 * Threshold: p95 < 500ms
 *
 * Usage:
 *   k6 run k6/scripts/board-list-load-test.js
 *   k6 run --env BASE_URL=http://localhost:8080 k6/scripts/board-list-load-test.js
 */

// Configuration
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const API_PREFIX = __ENV.API_PREFIX || '/api';

// Custom metrics
const boardListRequests = new Counter('board_list_requests');
const boardListErrors = new Rate('board_list_errors');
const boardListDuration = new Trend('board_list_duration');

export const options = {
    scenarios: {
        board_list_load: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '30s', target: 50 },   // Ramp up to 50 VUs
                { duration: '1m', target: 100 },   // Stay at 100 VUs
                { duration: '2m', target: 100 },   // Continue at 100 VUs
                { duration: '30s', target: 50 },   // Ramp down to 50
                { duration: '30s', target: 0 },    // Ramp down to 0
            ],
            gracefulRampDown: '10s',
        },
    },
    thresholds: {
        'http_req_duration': ['p(95)<500', 'p(99)<1000'],
        'http_req_failed': ['rate<0.05'],
        'board_list_errors': ['rate<0.05'],
        'board_list_duration': ['p(95)<500'],
    },
};

// Test data - category IDs to rotate through
const categoryIds = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];

export function setup() {
    console.log(`Testing against: ${BASE_URL}`);

    // Verify server is reachable
    const healthCheck = http.get(`${BASE_URL}/actuator/health`, {
        timeout: '5s',
    });

    if (healthCheck.status !== 200) {
        console.warn('Health check failed, server may not be ready');
    }

    return {
        categoryIds: categoryIds,
    };
}

export default function (data) {
    // Rotate through different category IDs
    const categoryId = data.categoryIds[Math.floor(Math.random() * data.categoryIds.length)];
    const pageNum = Math.floor(Math.random() * 5) + 1;  // Pages 1-5
    const pageSize = 20;

    // Build request URL - adjust based on your actual API structure
    const url = `${BASE_URL}${API_PREFIX}/boards?categoryId=${categoryId}&pageNum=${pageNum}&pageSize=${pageSize}`;

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
        },
        tags: { name: 'BoardList' },
    };

    const startTime = Date.now();
    const response = http.get(url, params);
    const duration = Date.now() - startTime;

    // Track metrics
    boardListRequests.add(1);
    boardListDuration.add(duration);

    // Validate response
    const success = check(response, {
        'status is 200': (r) => r.status === 200,
        'response time < 500ms': (r) => r.timings.duration < 500,
        'response has content': (r) => r.body && r.body.length > 0,
        'response is JSON': (r) => {
            try {
                JSON.parse(r.body);
                return true;
            } catch (e) {
                return false;
            }
        },
        'has content array': (r) => {
            try {
                const json = JSON.parse(r.body);
                return json.data && Array.isArray(json.data.content);
            } catch (e) {
                return false;
            }
        },
    });

    if (!success) {
        boardListErrors.add(1);
        console.error(`Request failed: ${response.status} - ${response.body}`);
    } else {
        boardListErrors.add(0);
    }

    // Think time - simulate user reading the list
    sleep(Math.random() * 2 + 1);  // 1-3 seconds
}

export function teardown(data) {
    console.log('Board List Load Test completed');
}

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

/**
 * Reply Pagination Load Test
 *
 * Tests: GET /reply/list/{postCode} (paginated replies)
 * Pattern: Ramping VUs (0 -> 40 -> 80 -> 0)
 * Threshold: p95 < 800ms
 *
 * Usage:
 *   k6 run k6/scripts/reply-pagination-load-test.js
 *   k6 run --env BASE_URL=http://localhost:8080 k6/scripts/reply-pagination-load-test.js
 */

// Configuration
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const API_PREFIX = __ENV.API_PREFIX || '/api';

// Custom metrics
const replyListRequests = new Counter('reply_list_requests');
const replyListErrors = new Rate('reply_list_errors');
const replyListDuration = new Trend('reply_list_duration');

export const options = {
    scenarios: {
        reply_pagination_load: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '30s', target: 40 },   // Ramp up to 40 VUs
                { duration: '1m', target: 80 },    // Stay at 80 VUs
                { duration: '2m', target: 80 },    // Continue at 80 VUs
                { duration: '30s', target: 40 },   // Ramp down to 40
                { duration: '30s', target: 0 },    // Ramp down to 0
            ],
            gracefulRampDown: '10s',
        },
    },
    thresholds: {
        'http_req_duration': ['p(95)<800', 'p(99)<1500'],
        'http_req_failed': ['rate<0.05'],
        'reply_list_errors': ['rate<0.05'],
        'reply_list_duration': ['p(95)<800'],
    },
};

let postCodes = [];

export function setup() {
    console.log(`Testing against: ${BASE_URL}`);

    // Fetch post codes that have replies
    const listResponse = http.get(`${BASE_URL}${API_PREFIX}/boards?pageNum=1&pageSize=50`, {
        timeout: '10s',
    });

    if (listResponse.status === 200) {
        try {
            const data = JSON.parse(listResponse.body);
            if (data.data && data.data.content) {
                postCodes = data.data.content.map(board => board.code);
                console.log(`Loaded ${postCodes.length} post codes for testing`);
            }
        } catch (e) {
            console.error('Failed to parse board list response');
        }
    }

    // Also try to get book review codes
    const reviewResponse = http.get(`${BASE_URL}${API_PREFIX}/book-reviews?pageNum=1&pageSize=50`, {
        timeout: '10s',
    });

    if (reviewResponse.status === 200) {
        try {
            const data = JSON.parse(reviewResponse.body);
            if (data.data && data.data.content) {
                const reviewCodes = data.data.content.map(review => review.code);
                postCodes = postCodes.concat(reviewCodes);
                console.log(`Total post codes: ${postCodes.length}`);
            }
        } catch (e) {
            // Ignore errors from book review endpoint
        }
    }

    // Fallback post codes
    if (postCodes.length === 0) {
        console.warn('Using fallback post codes');
        postCodes = [
            'BO_1', 'BO_2', 'BO_3', 'BO_4', 'BO_5',
            'BR_1', 'BR_2', 'BR_3', 'BR_4', 'BR_5'
        ];
    }

    return {
        postCodes: postCodes,
    };
}

export default function (data) {
    // Select a random post code
    const postCode = data.postCodes[Math.floor(Math.random() * data.postCodes.length)];
    const pageNum = Math.floor(Math.random() * 3) + 1;  // Pages 1-3
    const pageSize = 20;

    // Build request URL - adjust based on your actual API structure
    const url = `${BASE_URL}${API_PREFIX}/replies/${postCode}?pageNum=${pageNum}&pageSize=${pageSize}`;

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
        },
        tags: { name: 'ReplyList' },
    };

    const startTime = Date.now();
    const response = http.get(url, params);
    const duration = Date.now() - startTime;

    // Track metrics
    replyListRequests.add(1);
    replyListDuration.add(duration);

    // Validate response
    const success = check(response, {
        'status is 200': (r) => r.status === 200,
        'response time < 800ms': (r) => r.timings.duration < 800,
        'response has content': (r) => r.body && r.body.length > 0,
        'response is JSON': (r) => {
            try {
                JSON.parse(r.body);
                return true;
            } catch (e) {
                return false;
            }
        },
        'has replies data': (r) => {
            try {
                const json = JSON.parse(r.body);
                return json.data && Array.isArray(json.data.content);
            } catch (e) {
                return false;
            }
        },
    });

    if (!success) {
        replyListErrors.add(1);
        if (response.status !== 200) {
            console.error(`Request failed: ${response.status} for postCode: ${postCode}`);
        }
    } else {
        replyListErrors.add(0);

        // Log reply tree depth for analysis
        try {
            const json = JSON.parse(response.body);
            if (json.data && json.data.content && json.data.content.length > 0) {
                const maxDepth = calculateMaxDepth(json.data.content);
                if (maxDepth > 3) {
                    console.warn(`Deep reply tree detected: depth=${maxDepth}`);
                }
            }
        } catch (e) {
            // Ignore parsing errors for logging
        }
    }

    // Think time
    sleep(Math.random() * 2 + 1);  // 1-3 seconds
}

function calculateMaxDepth(replies, currentDepth = 1) {
    if (!replies || replies.length === 0) {
        return currentDepth - 1;
    }

    let maxDepth = currentDepth;
    for (const reply of replies) {
        if (reply.replies && reply.replies.length > 0) {
            const childDepth = calculateMaxDepth(reply.replies, currentDepth + 1);
            maxDepth = Math.max(maxDepth, childDepth);
        }
    }
    return maxDepth;
}

export function teardown(data) {
    console.log('Reply Pagination Load Test completed');
}

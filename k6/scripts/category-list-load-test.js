import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

/**
 * Category List Load Test
 *
 * Tests: GET /community/category/list (or /api/categories)
 * Pattern: Constant VUs (50)
 * Threshold: p95 < 200ms
 *
 * Categories are frequently fetched for navigation, so this should be very fast.
 *
 * Usage:
 *   k6 run k6/scripts/category-list-load-test.js
 *   k6 run --env BASE_URL=http://localhost:8080 k6/scripts/category-list-load-test.js
 */

// Configuration
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const API_PREFIX = __ENV.API_PREFIX || '/api';

// Custom metrics
const categoryListRequests = new Counter('category_list_requests');
const categoryListErrors = new Rate('category_list_errors');
const categoryListDuration = new Trend('category_list_duration');

export const options = {
    scenarios: {
        category_list_constant: {
            executor: 'constant-vus',
            vus: 50,
            duration: '3m',
        },
    },
    thresholds: {
        'http_req_duration': ['p(95)<200', 'p(99)<500'],
        'http_req_failed': ['rate<0.01'],
        'category_list_errors': ['rate<0.01'],
        'category_list_duration': ['p(95)<200'],
    },
};

export function setup() {
    console.log(`Testing against: ${BASE_URL}`);

    // Verify server is reachable
    const healthCheck = http.get(`${BASE_URL}/actuator/health`, {
        timeout: '5s',
    });

    if (healthCheck.status !== 200) {
        console.warn('Health check failed, server may not be ready');
    }

    return {};
}

export default function () {
    // Build request URL
    const url = `${BASE_URL}${API_PREFIX}/categories`;

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
        },
        tags: { name: 'CategoryList' },
    };

    const startTime = Date.now();
    const response = http.get(url, params);
    const duration = Date.now() - startTime;

    // Track metrics
    categoryListRequests.add(1);
    categoryListDuration.add(duration);

    // Validate response
    const success = check(response, {
        'status is 200': (r) => r.status === 200,
        'response time < 200ms': (r) => r.timings.duration < 200,
        'response has content': (r) => r.body && r.body.length > 0,
        'response is JSON': (r) => {
            try {
                JSON.parse(r.body);
                return true;
            } catch (e) {
                return false;
            }
        },
        'has categories array': (r) => {
            try {
                const json = JSON.parse(r.body);
                // Handle both direct array and wrapped response
                return Array.isArray(json) ||
                       (json.data && Array.isArray(json.data)) ||
                       (json.data && Array.isArray(json.data.content));
            } catch (e) {
                return false;
            }
        },
        'has multiple categories': (r) => {
            try {
                const json = JSON.parse(r.body);
                const categories = Array.isArray(json) ? json :
                                   (json.data && Array.isArray(json.data)) ? json.data :
                                   (json.data && json.data.content) ? json.data.content : [];
                return categories.length > 0;
            } catch (e) {
                return false;
            }
        },
    });

    if (!success) {
        categoryListErrors.add(1);
        console.error(`Request failed: ${response.status} - ${response.body}`);
    } else {
        categoryListErrors.add(0);
    }

    // Very short think time for navigation requests
    sleep(Math.random() * 0.5 + 0.2);  // 0.2-0.7 seconds
}

export function teardown(data) {
    console.log('Category List Load Test completed');
}

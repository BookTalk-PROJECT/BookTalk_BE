import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

/**
 * Mixed Workload Load Test
 *
 * Simulates realistic traffic pattern with multiple API endpoints
 *
 * Traffic Distribution:
 * - Board List: 40%
 * - Board Detail: 25%
 * - Reply List: 20%
 * - Category List: 15%
 *
 * Pattern: Ramping VUs (0 -> 50 -> 100 -> 150 -> 200 -> 0)
 * Threshold: p95 < 1000ms overall
 *
 * Usage:
 *   k6 run k6/scripts/mixed-workload-test.js
 *   k6 run --env BASE_URL=http://localhost:8080 k6/scripts/mixed-workload-test.js
 */

// Configuration
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const API_PREFIX = __ENV.API_PREFIX || '/api';

// Traffic weight percentages
const TRAFFIC_WEIGHTS = {
    boardList: 40,      // 40%
    boardDetail: 25,    // 25%
    replyList: 20,      // 20%
    categoryList: 15,   // 15%
};

// Custom metrics per endpoint
const metrics = {
    boardList: {
        requests: new Counter('board_list_requests'),
        errors: new Rate('board_list_errors'),
        duration: new Trend('board_list_duration'),
    },
    boardDetail: {
        requests: new Counter('board_detail_requests'),
        errors: new Rate('board_detail_errors'),
        duration: new Trend('board_detail_duration'),
    },
    replyList: {
        requests: new Counter('reply_list_requests'),
        errors: new Rate('reply_list_errors'),
        duration: new Trend('reply_list_duration'),
    },
    categoryList: {
        requests: new Counter('category_list_requests'),
        errors: new Rate('category_list_errors'),
        duration: new Trend('category_list_duration'),
    },
};

// Overall metrics
const totalRequests = new Counter('total_requests');
const totalErrors = new Rate('total_errors');
const overallDuration = new Trend('overall_duration');

export const options = {
    scenarios: {
        mixed_workload: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '1m', target: 50 },    // Warm up
                { duration: '2m', target: 100 },   // Ramp up
                { duration: '3m', target: 150 },   // Peak load
                { duration: '2m', target: 200 },   // Max load
                { duration: '2m', target: 100 },   // Ramp down
                { duration: '1m', target: 0 },     // Cool down
            ],
            gracefulRampDown: '30s',
        },
    },
    thresholds: {
        'http_req_duration': ['p(95)<1000', 'p(99)<2000'],
        'http_req_failed': ['rate<0.05'],
        'total_errors': ['rate<0.05'],
        'overall_duration': ['p(95)<1000'],
        'board_list_duration': ['p(95)<500'],
        'board_detail_duration': ['p(95)<1000'],
        'reply_list_duration': ['p(95)<800'],
        'category_list_duration': ['p(95)<200'],
    },
};

// Test data
let testData = {
    categoryIds: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
    boardCodes: [],
    postCodes: [],
};

export function setup() {
    console.log(`Testing against: ${BASE_URL}`);
    console.log('Traffic distribution:', TRAFFIC_WEIGHTS);

    // Load board codes for detail and reply tests
    const listResponse = http.get(`${BASE_URL}${API_PREFIX}/boards?pageNum=1&pageSize=100`, {
        timeout: '10s',
    });

    if (listResponse.status === 200) {
        try {
            const data = JSON.parse(listResponse.body);
            if (data.data && data.data.content) {
                testData.boardCodes = data.data.content.map(board => board.code);
                testData.postCodes = [...testData.boardCodes];
                console.log(`Loaded ${testData.boardCodes.length} board codes`);
            }
        } catch (e) {
            console.error('Failed to load board codes');
        }
    }

    // Fallback data
    if (testData.boardCodes.length === 0) {
        testData.boardCodes = ['BO_1', 'BO_2', 'BO_3', 'BO_4', 'BO_5'];
        testData.postCodes = [...testData.boardCodes];
    }

    return testData;
}

export default function (data) {
    // Select endpoint based on traffic weights
    const random = Math.random() * 100;
    let endpoint;

    if (random < TRAFFIC_WEIGHTS.boardList) {
        endpoint = 'boardList';
        boardListRequest(data);
    } else if (random < TRAFFIC_WEIGHTS.boardList + TRAFFIC_WEIGHTS.boardDetail) {
        endpoint = 'boardDetail';
        boardDetailRequest(data);
    } else if (random < TRAFFIC_WEIGHTS.boardList + TRAFFIC_WEIGHTS.boardDetail + TRAFFIC_WEIGHTS.replyList) {
        endpoint = 'replyList';
        replyListRequest(data);
    } else {
        endpoint = 'categoryList';
        categoryListRequest(data);
    }

    totalRequests.add(1);
}

function boardListRequest(data) {
    const categoryId = data.categoryIds[Math.floor(Math.random() * data.categoryIds.length)];
    const pageNum = Math.floor(Math.random() * 5) + 1;
    const url = `${BASE_URL}${API_PREFIX}/boards?categoryId=${categoryId}&pageNum=${pageNum}&pageSize=20`;

    const params = {
        headers: { 'Accept': 'application/json' },
        tags: { name: 'BoardList', endpoint: 'board_list' },
    };

    const startTime = Date.now();
    const response = http.get(url, params);
    const duration = Date.now() - startTime;

    metrics.boardList.requests.add(1);
    metrics.boardList.duration.add(duration);
    overallDuration.add(duration);

    const success = check(response, {
        'boardList: status 200': (r) => r.status === 200,
        'boardList: response < 500ms': (r) => r.timings.duration < 500,
    });

    if (!success) {
        metrics.boardList.errors.add(1);
        totalErrors.add(1);
    } else {
        metrics.boardList.errors.add(0);
        totalErrors.add(0);
    }

    sleep(Math.random() * 2 + 1);
}

function boardDetailRequest(data) {
    const boardCode = data.boardCodes[Math.floor(Math.random() * data.boardCodes.length)];
    const url = `${BASE_URL}${API_PREFIX}/boards/${boardCode}`;

    const params = {
        headers: { 'Accept': 'application/json' },
        tags: { name: 'BoardDetail', endpoint: 'board_detail' },
    };

    const startTime = Date.now();
    const response = http.get(url, params);
    const duration = Date.now() - startTime;

    metrics.boardDetail.requests.add(1);
    metrics.boardDetail.duration.add(duration);
    overallDuration.add(duration);

    const success = check(response, {
        'boardDetail: status 200 or 404': (r) => r.status === 200 || r.status === 404,
        'boardDetail: response < 1000ms': (r) => r.timings.duration < 1000,
    });

    if (!success) {
        metrics.boardDetail.errors.add(1);
        totalErrors.add(1);
    } else {
        metrics.boardDetail.errors.add(0);
        totalErrors.add(0);
    }

    sleep(Math.random() * 3 + 2);
}

function replyListRequest(data) {
    const postCode = data.postCodes[Math.floor(Math.random() * data.postCodes.length)];
    const pageNum = Math.floor(Math.random() * 3) + 1;
    const url = `${BASE_URL}${API_PREFIX}/replies/${postCode}?pageNum=${pageNum}&pageSize=20`;

    const params = {
        headers: { 'Accept': 'application/json' },
        tags: { name: 'ReplyList', endpoint: 'reply_list' },
    };

    const startTime = Date.now();
    const response = http.get(url, params);
    const duration = Date.now() - startTime;

    metrics.replyList.requests.add(1);
    metrics.replyList.duration.add(duration);
    overallDuration.add(duration);

    const success = check(response, {
        'replyList: status 200': (r) => r.status === 200,
        'replyList: response < 800ms': (r) => r.timings.duration < 800,
    });

    if (!success) {
        metrics.replyList.errors.add(1);
        totalErrors.add(1);
    } else {
        metrics.replyList.errors.add(0);
        totalErrors.add(0);
    }

    sleep(Math.random() * 2 + 1);
}

function categoryListRequest(data) {
    const url = `${BASE_URL}${API_PREFIX}/categories`;

    const params = {
        headers: { 'Accept': 'application/json' },
        tags: { name: 'CategoryList', endpoint: 'category_list' },
    };

    const startTime = Date.now();
    const response = http.get(url, params);
    const duration = Date.now() - startTime;

    metrics.categoryList.requests.add(1);
    metrics.categoryList.duration.add(duration);
    overallDuration.add(duration);

    const success = check(response, {
        'categoryList: status 200': (r) => r.status === 200,
        'categoryList: response < 200ms': (r) => r.timings.duration < 200,
    });

    if (!success) {
        metrics.categoryList.errors.add(1);
        totalErrors.add(1);
    } else {
        metrics.categoryList.errors.add(0);
        totalErrors.add(0);
    }

    sleep(Math.random() * 0.5 + 0.2);
}

export function teardown(data) {
    console.log('Mixed Workload Load Test completed');
    console.log('\nExpected traffic distribution:');
    console.log(`  Board List: ${TRAFFIC_WEIGHTS.boardList}%`);
    console.log(`  Board Detail: ${TRAFFIC_WEIGHTS.boardDetail}%`);
    console.log(`  Reply List: ${TRAFFIC_WEIGHTS.replyList}%`);
    console.log(`  Category List: ${TRAFFIC_WEIGHTS.categoryList}%`);
}

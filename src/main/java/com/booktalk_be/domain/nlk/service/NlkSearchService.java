package com.booktalk_be.domain.nlk.service;

import com.booktalk_be.domain.nlk.responseDto.NlkBookDto;
import com.booktalk_be.domain.nlk.responseDto.NlkSearchResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NlkSearchService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${nlk.api.key}")
    private String apiKey;

    @Value("${nlk.api.base-url}")
    private String baseUrl;

    public NlkSearchResponse search(String kwd, int pageNum, int pageSize) {
        if (!StringUtils.hasText(kwd)) {
            return NlkSearchResponse.builder()
                    .total(0)
                    .pageNum(pageNum)
                    .pageSize(pageSize)
                    .items(List.of())
                    .build();
        }

        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("key", apiKey)
                .queryParam("apiType", "json")
                .queryParam("srchTarget", "title")
                .queryParam("kwd", kwd)
                .queryParam("pageNum", pageNum)
                .queryParam("pageSize", pageSize)
                .encode()      // 한글 파라미터 인코딩
                .build()
                .toUri();

        try {
            // ★ text/json 때문에 JsonNode로 직접 못 받으니, 먼저 String으로 받는다
            String body = restTemplate.getForObject(uri, String.class);
            if (!StringUtils.hasText(body)) {
                return NlkSearchResponse.builder()
                        .total(0)
                        .pageNum(pageNum)
                        .pageSize(pageSize)
                        .items(List.of())
                        .build();
            }

            JsonNode root = objectMapper.readTree(body);

            int total = root.path("result").path("total").asInt(
                    root.path("total").asInt(0)
            );

            JsonNode itemsNode = findItemsNode(root);

            List<NlkBookDto> items = new ArrayList<>();
            if (itemsNode != null && itemsNode.isArray()) {
                for (JsonNode it : itemsNode) {
                    String rawTitle = firstNonEmpty(it,
                            "title_info", "titleInfo", "title", "TITLE");
                    String title = stripHtml(rawTitle);

                    String rawIsbn = firstNonEmpty(it, "isbn", "ISBN");
                    String isbn = rawIsbn == null ? "" : rawIsbn.replaceAll("[^0-9Xx]", "");

                    String author = stripHtml(firstNonEmpty(it,
                            "author_info", "authorInfo", "author", "AUTHOR"));

                    String year = stripHtml(firstNonEmpty(it,
                            "pub_year_info", "pubYearInfo", "pub_year", "PUB_YEAR"));

                    String cover = firstNonEmpty(it, "image_url", "IMAGE_URL", "bookImageURL");

                    String id = !isbn.isEmpty()
                            ? isbn
                            : firstNonEmpty(it, "control_no", "controlNo", "id");

                    if (!StringUtils.hasText(title)) {
                        continue;
                    }

                    items.add(NlkBookDto.builder()
                            .id(id)
                            .title(title)
                            .isbn(isbn)
                            .author(author)
                            .year(year)
                            .cover(cover)
                            .build());
                }
            }

            return NlkSearchResponse.builder()
                    .total(total)
                    .pageNum(pageNum)
                    .pageSize(pageSize)
                    .items(items)
                    .build();

        } catch (Exception e) {
            // 필요하면 로깅 추가
            throw new RuntimeException("NLK 검색 호출 실패", e);
        }
    }

    private JsonNode findItemsNode(JsonNode root) {
        JsonNode result = root.path("result");
        if (result.isArray()) return result;
        if (result.path("items").isArray()) return result.path("items");

        if (root.path("items").isArray()) return root.path("items");
        if (root.path("docs").isArray()) return root.path("docs");
        if (root.path("channel").path("item").isArray()) return root.path("channel").path("item");

        return null;
    }

    private String firstNonEmpty(JsonNode node, String... names) {
        for (String name : names) {
            JsonNode v = node.get(name);
            if (v != null && !v.isNull()) {
                String s = v.asText();
                if (StringUtils.hasText(s)) return s;
            }
        }
        return "";
    }

    private String stripHtml(String html) {
        if (!StringUtils.hasText(html)) return "";
        return html.replaceAll("<[^>]*>", "").trim();
    }
}
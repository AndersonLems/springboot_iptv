package com.anderson.iptv.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.anderson.iptv.config.AppProperties;
import com.anderson.iptv.model.EpgProgram;
import com.anderson.iptv.services.EpgService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;

class EpgParserTest {

    @Test
    void testParseValidXmltvFile() {
        String xml = """
                <tv>
                  <programme channel="ch1" start="20240101120000 +0000" stop="20240101130000 +0000">
                    <title>Programa 1</title>
                    <desc>Descricao 1</desc>
                  </programme>
                </tv>
                """;
        EpgParser parser = new EpgParser();
        Map<String, List<EpgProgram>> result = parser.parse(
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        assertEquals(1, result.get("ch1").size());
        assertEquals("Programa 1", result.get("ch1").get(0).getTitle());
    }

    @Test
    void testNowReturnsCurrentlyAiringProgram() {
        EpgService service = Mockito.spy(new EpgService(WebClient.builder().build(), new AppProperties(), new EpgParser()));
        Instant now = Instant.now();
        EpgProgram current = EpgProgram.builder()
                .channelId("ch1")
                .title("Agora")
                .startTime(now.minusSeconds(60))
                .endTime(now.plusSeconds(60))
                .build();
        Mockito.doReturn(Map.of("ch1", List.of(current))).when(service).loadEpg();

        EpgProgram nowProgram = service.getNow("ch1");
        assertEquals("Agora", nowProgram.getTitle());
        assertTrue(nowProgram.isCurrentlyAiring());
    }

    @Test
    void testNextReturnsNextProgram() {
        EpgService service = Mockito.spy(new EpgService(WebClient.builder().build(), new AppProperties(), new EpgParser()));
        Instant now = Instant.now();
        EpgProgram next = EpgProgram.builder()
                .channelId("ch1")
                .title("Depois")
                .startTime(now.plusSeconds(3600))
                .endTime(now.plusSeconds(7200))
                .build();
        Mockito.doReturn(Map.of("ch1", List.of(next))).when(service).loadEpg();

        EpgProgram nextProgram = service.getNext("ch1");
        assertEquals("Depois", nextProgram.getTitle());
    }

    @Test
    void testEmptyEpgReturnsEmptyList() {
        EpgParser parser = new EpgParser();
        Map<String, List<EpgProgram>> result = parser.parse(
                new ByteArrayInputStream("<tv></tv>".getBytes(StandardCharsets.UTF_8)));
        assertEquals(0, result.size());
    }

    @Test
    void testEpgNotConfiguredReturns503() {
        AppProperties props = new AppProperties();
        props.getEpg().setUrl("");
        EpgService service = new EpgService(WebClient.builder().build(), props, new EpgParser());
        assertThrows(ResponseStatusException.class, service::loadEpg);
    }
}

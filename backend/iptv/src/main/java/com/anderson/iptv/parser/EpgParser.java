package com.anderson.iptv.parser;

import com.anderson.iptv.model.EpgProgram;

import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.springframework.stereotype.Component;

@Component
public class EpgParser {

    private static final DateTimeFormatter XMLTV_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss Z");

    public Map<String, List<EpgProgram>> parse(InputStream input) {
        Map<String, List<EpgProgram>> byChannel = new HashMap<>();
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader reader = factory.createXMLStreamReader(input);
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT && "programme".equals(reader.getLocalName())) {
                    String channelId = reader.getAttributeValue(null, "channel");
                    String start = reader.getAttributeValue(null, "start");
                    String stop = reader.getAttributeValue(null, "stop");

                    String title = null;
                    String desc = null;
                    while (reader.hasNext()) {
                        int inner = reader.next();
                        if (inner == XMLStreamConstants.START_ELEMENT && "title".equals(reader.getLocalName())) {
                            title = reader.getElementText();
                        } else if (inner == XMLStreamConstants.START_ELEMENT && "desc".equals(reader.getLocalName())) {
                            desc = reader.getElementText();
                        } else if (inner == XMLStreamConstants.END_ELEMENT && "programme".equals(reader.getLocalName())) {
                            break;
                        }
                    }

                    Instant startTime = parseTime(start);
                    Instant endTime = parseTime(stop);
                    EpgProgram program = EpgProgram.builder()
                            .channelId(channelId)
                            .title(title)
                            .description(desc)
                            .startTime(startTime)
                            .endTime(endTime)
                            .isCurrentlyAiring(false)
                            .build();

                    byChannel.computeIfAbsent(channelId, k -> new ArrayList<>()).add(program);
                }
            }
            reader.close();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar XMLTV", e);
        }
        return byChannel;
    }

    private Instant parseTime(String raw) {
        if (raw == null || raw.isBlank()) {
            return Instant.EPOCH;
        }
        try {
            return ZonedDateTime.parse(raw.trim(), XMLTV_FORMAT).toInstant();
        } catch (Exception e) {
            return ZonedDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC).toInstant();
        }
    }
}

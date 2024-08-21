package in.javis.toolserver.pojo;

import lombok.Data;

import java.util.List;

@Data
public class EmbedS3FileRequest {
    private List<Record> records;

    @Data
    public static class Record {
        private String bucket;
        private String key;
    }
}
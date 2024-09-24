package cn.colin.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MinioFile {
    private String bucketName;
    private String fileName;
    private Long size;
    private Date lastModifiedTime;
}

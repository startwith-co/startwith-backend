package startwithco.startwithbackend.common.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

import static java.util.UUID.randomUUID;

@Component
public class CommonService {
    private AmazonS3 s3client;
    private String bucketName;

    public CommonService(@Value("${cloud.aws.s3.bucket}") String bucketName, AmazonS3 s3client) {
        this.bucketName = bucketName;
        this.s3client = s3client;
    }

    public String uploadJPGFile(MultipartFile multipartFile) throws IOException {
        String fileName = randomUUID().toString() + ".jpg";
        InputStream inputStream = multipartFile.getInputStream();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());

        s3client.putObject(new PutObjectRequest(bucketName, fileName, inputStream, metadata));
        return s3client.getUrl(bucketName, fileName).toString();
    }

    public String uploadPDFFile(MultipartFile pdfFile) throws IOException {
        String fileName = randomUUID().toString() + ".pdf";
        InputStream inputStream = pdfFile.getInputStream();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(pdfFile.getSize());
        metadata.setContentType("application/pdf");

        s3client.putObject(new PutObjectRequest(bucketName, fileName, inputStream, metadata));
        return s3client.getUrl(bucketName, fileName).toString();
    }
}

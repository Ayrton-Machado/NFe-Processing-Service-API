package com.erpservices.nfe.s3.service;

import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

@ApplicationScoped
public class S3Service {

    @ConfigProperty(name = "bucket.name")
    private String bucketName;

    @Inject
    private S3Client s3;

    public PutObjectResponse uploadXmlToS3(byte[] xml, String chaveAcesso) {
        String nomeArquivo = "nfe/" + chaveAcesso + ".xml";
        return s3.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(nomeArquivo)
                        .contentType("application/xml")
                        .build(), RequestBody.fromBytes(xml));
    }

    public List<S3Object> listS3Objects() {
        return s3.listObjects(ListObjectsRequest.builder().bucket(bucketName).build()).contents();
    }

    public List<String> listXmlInS3() {
        return listS3Objects().stream()
            .map(S3Object::key)
            .toList();
    }
}

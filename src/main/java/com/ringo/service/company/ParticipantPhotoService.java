package com.ringo.service.company;

import org.springframework.stereotype.Service;

@Service
public class ParticipantPhotoService {

//    public ParticipantPhotoService(AwsFileManager awsFileManager, ParticipantPhotoRepository repository) {
//        super(awsFileManager, repository, ParticipantPhoto.class);
//    }
//
//    @Override
//    protected byte[] processPhoto(byte[] bytes, String contentType) {
//        return bytes;
//    }
//
//    @Override
//    protected String getPath(ParticipantPhoto photoEntity, MultipartFile photo) {
//        return "participant#" + photoEntity.getOwner().getId() + "_profile." + photo.getContentType().split("/")[1];
//    }
//
//    @Override
//    protected ParticipantPhoto configurePhoto(ParticipantPhoto photoEntity, byte[] bytes, MultipartFile file) {
//        if(photoEntity.getOwner().getProfilePicture() != null) {
//            awsFileManager.deleteFile(photoEntity.getOwner().getProfilePicture().getPath());
//        }
//        return photoEntity;
//    }
}

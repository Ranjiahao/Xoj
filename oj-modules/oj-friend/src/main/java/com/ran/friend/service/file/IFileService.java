package com.ran.friend.service.file;

import com.ran.common.file.domain.OSSResult;
import org.springframework.web.multipart.MultipartFile;

public interface IFileService {
    OSSResult upload(MultipartFile file);
}
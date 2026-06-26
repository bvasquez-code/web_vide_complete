package com.ccadmin.app.video.model.dto;

import java.util.ArrayList;
import java.util.List;

public class VideoCaptureCleanupResultDto {
    public Integer LinkedFileCount = 0;
    public Integer ScannedFileCount = 0;
    public Integer DeletedFileCount = 0;
    public Integer ErrorCount = 0;
    public Boolean DryRun = false;
    public List<String> DeletedFiles = new ArrayList<>();
    public List<String> Errors = new ArrayList<>();
}

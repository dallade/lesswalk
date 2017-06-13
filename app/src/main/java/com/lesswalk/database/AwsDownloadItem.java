package com.lesswalk.database;

import com.amazonaws.services.s3.model.ObjectMetadata;

/**
 * Created by elazarkin on 5/21/17.
 */

public class AwsDownloadItem
{
    private String         filePath     = null;
    private String         onServerPath = null;
    private ObjectMetadata metadata     = null;

    public AwsDownloadItem(String filePath, String onServerPath, ObjectMetadata metadata)
    {
        this.filePath = filePath;
        this.onServerPath = onServerPath;
        this.metadata = metadata;
    }

    public AwsDownloadItem clone()
    {
        return new AwsDownloadItem(filePath, onServerPath, metadata);
    }

    public String getFilePath()
    {
        return filePath;
    }

    public String getOnServerPath()
    {
        return onServerPath;
    }

    public ObjectMetadata getMetadata()
    {
        return metadata;
    }
}

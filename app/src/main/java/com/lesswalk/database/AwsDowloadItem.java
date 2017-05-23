package com.lesswalk.database;

import com.amazonaws.services.s3.model.ObjectMetadata;

/**
 * Created by elazarkin on 5/21/17.
 */

public class AwsDowloadItem
{
    private String         filePath     = null;
    private String         onServerPath = null;
    private ObjectMetadata metadata     = null;

    public AwsDowloadItem(String filePath, String onServerPath, ObjectMetadata metadata)
    {
        this.filePath = filePath;
        this.onServerPath = onServerPath;
        this.metadata = metadata;
    }

    public AwsDowloadItem clone()
    {
        return new AwsDowloadItem(filePath, onServerPath, metadata);
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

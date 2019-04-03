/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.canvasapi.model.kaltura;

import android.os.Parcel;
import android.os.Parcelable;

import org.simpleframework.xml.Element;

@Element
public class Result implements Parcelable {

    /*
    <objectType>KalturaMediaEntry</objectType>
      <id></id>
      <name></name>
      <description />
      <partnerId></partnerId>
      <userId></userId>
      <tags />
      <adminTags />
      <categories />
      <catoriesIds/>
      <status></status>
      <moderationStatus></moderationStatus>
      <moderationCount></moderationCount>
      <type></type>
      <createdAt></createdAt>
      <rank></rank>
      <totalRank></totalRank>
      <votes></votes>
      <groupId />
      <partnerData />
      <downloadUrl></downloadUrl>
      <searchText></searchText>
      <licenseType></licenseType>
      <version></version>
      <thumbnailUrl></thumbnailUrl>
      <accessControlId></accessControlId>
      <startDate />
      <endDate />
      <plays></plays>
      <views></views>
      <width />
      <height />
      <duration></duration>
      <msDuration></msDuration>
      <durationType />
      <mediaType></mediaType>
      <conversionQuality />
      <sourceType></sourceType>
      <searchProviderType />
      <searchProviderId />
      <creditUserName />
      <creditUrl />
      <mediaDate />
      <dataUrl></dataUrl>
      <flavorParamsIds />
     */

    public static Parcelable.Creator<Result> CREATOR = new Parcelable.Creator<Result>() {
        public Result createFromParcel(Parcel source) {
            return new Result(source);
        }

        public Result[] newArray(int size) {
            return new Result[size];
        }
    };
    @Element(required = false)
    private String objectType;
    @Element(required = false)
    private String id;
    @Element(required = false)
    private long partnerId;
    @Element(required = false)
    private String userId;
    @Element(required = false)
    private String status;
    @Element(required = false)
    private String fileName;
    @Element(required = false)
    private String fileSize;
    @Element(required = false)
    private long uploadedFileSize;
    @Element(required = false)
    private long createdAt;
    @Element(required = false)
    private long updatedAt;
    @Element(required = false)
    private String name;
    @Element(required = false)
    private String description;
    @Element(required = false)
    private String tags;
    @Element(required = false)
    private String adminTags;
    @Element(required = false)
    private String categories;
    @Element(required = false)
    private String partnerData;
    @Element(required = false)
    private String downloadUrl;
    @Element(required = false)
    private long moderationStatus;
    @Element(required = false)
    private long moderationCount;
    @Element(required = false)
    private long type;
    @Element(required = false)
    private long totalRank;
    @Element(required = false)
    private long rank;
    @Element(required = false)
    private long votes;
    @Element(required = false)
    private long groupId;
    @Element(required = false)
    private String searchText;
    @Element(required = false)
    private long licenseType;
    @Element(required = false)
    private long version;
    @Element(required = false)
    private String thumbnailUrl;
    @Element(required = false)
    private long accessControlId;
    @Element(required = false)
    private long startDate;
    @Element(required = false)
    private long endDate;
    @Element(required = false)
    private long plays;
    @Element(required = false)
    private long views;
    @Element(required = false)
    private long width;
    @Element(required = false)
    private long height;
    @Element(required = false)
    private double duration;
    @Element(required = false)
    private long durationType;
    @Element(required = false)
    private long mediaType;
    @Element(required = false)
    private long conversionQuality;
    @Element(required = false)
    private long sourceType;
    @Element(required = false)
    private long searchProviderType;
    @Element(required = false)
    private long searchProviderId;
    @Element(required = false)
    private String creditUserName;
    @Element(required = false)
    private String creditUrl;
    @Element(required = false)
    private String mediaDate;
    @Element(required = false)
    private String dataUrl;
    @Element(required = false)
    private String flavorParamsIds;
    @Element(required = false)
    //misspelled on Kaltura's side
    private String catoriesIds;
    @Element(required = false)
    private double msDuration;
    @Element(required = false)
    private Error error;

    public Result() {
    }

    private Result(Parcel in) {
        this.objectType = in.readString();
        this.id = in.readString();
        this.partnerId = in.readLong();
        this.userId = in.readString();
        this.status = in.readString();
        this.fileName = in.readString();
        this.fileSize = in.readString();
        this.uploadedFileSize = in.readLong();
        this.createdAt = in.readLong();
        this.updatedAt = in.readLong();
        this.name = in.readString();
        this.description = in.readString();
        this.tags = in.readString();
        this.adminTags = in.readString();
        this.categories = in.readString();
        this.partnerData = in.readString();
        this.downloadUrl = in.readString();
        this.moderationStatus = in.readLong();
        this.moderationCount = in.readLong();
        this.type = in.readLong();
        this.totalRank = in.readLong();
        this.rank = in.readLong();
        this.votes = in.readLong();
        this.groupId = in.readLong();
        this.searchText = in.readString();
        this.licenseType = in.readLong();
        this.version = in.readLong();
        this.thumbnailUrl = in.readString();
        this.accessControlId = in.readLong();
        this.startDate = in.readLong();
        this.endDate = in.readLong();
        this.plays = in.readLong();
        this.views = in.readLong();
        this.width = in.readLong();
        this.height = in.readLong();
        this.duration = in.readDouble();
        this.durationType = in.readLong();
        this.mediaType = in.readLong();
        this.conversionQuality = in.readLong();
        this.sourceType = in.readLong();
        this.searchProviderType = in.readLong();
        this.searchProviderId = in.readLong();
        this.creditUserName = in.readString();
        this.creditUrl = in.readString();
        this.mediaDate = in.readString();
        this.dataUrl = in.readString();
        this.flavorParamsIds = in.readString();
        this.catoriesIds = in.readString();
        this.msDuration = in.readDouble();
        this.error = in.readParcelable(Error.class.getClassLoader());
    }

    public Error getKalturaError() {
        return error;
    }

    public void setKalturaError(Error kalturaError) {
        this.error = kalturaError;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(long partnerId) {
        this.partnerId = partnerId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public long getUploadedFileSize() {
        return uploadedFileSize;
    }

    public void setUploadedFileSize(long uploadedFileSize) {
        this.uploadedFileSize = uploadedFileSize;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getAdminTags() {
        return adminTags;
    }

    public void setAdminTags(String adminTags) {
        this.adminTags = adminTags;
    }

    public String getCategories() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }

    public String getPartnerData() {
        return partnerData;
    }

    public void setPartnerData(String partnerData) {
        this.partnerData = partnerData;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public long getModerationStatus() {
        return moderationStatus;
    }

    public void setModerationStatus(long moderationStatus) {
        this.moderationStatus = moderationStatus;
    }

    public long getModerationCount() {
        return moderationCount;
    }

    public void setModerationCount(long moderationCount) {
        this.moderationCount = moderationCount;
    }

    public long getType() {
        return type;
    }

    public void setType(long type) {
        this.type = type;
    }

    public long getTotalRank() {
        return totalRank;
    }

    public void setTotalRank(long totalRank) {
        this.totalRank = totalRank;
    }

    public long getRank() {
        return rank;
    }

    public void setRank(long rank) {
        this.rank = rank;
    }

    public long getVotes() {
        return votes;
    }

    public void setVotes(long votes) {
        this.votes = votes;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public long getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(long licenseType) {
        this.licenseType = licenseType;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public long getAccessControlId() {
        return accessControlId;
    }

    public void setAccessControlId(long accessControlId) {
        this.accessControlId = accessControlId;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public long getPlays() {
        return plays;
    }

    public void setPlays(long plays) {
        this.plays = plays;
    }

    public long getViews() {
        return views;
    }

    public void setViews(long views) {
        this.views = views;
    }

    public long getWidth() {
        return width;
    }

    public void setWidth(long width) {
        this.width = width;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public long getDurationType() {
        return durationType;
    }

    public void setDurationType(long durationType) {
        this.durationType = durationType;
    }

    public long getMediaType() {
        return mediaType;
    }

    public void setMediaType(long mediaType) {
        this.mediaType = mediaType;
    }

    public long getConversionQuality() {
        return conversionQuality;
    }

    public void setConversionQuality(long conversionQuality) {
        this.conversionQuality = conversionQuality;
    }

    public long getSourceType() {
        return sourceType;
    }

    public void setSourceType(long sourceType) {
        this.sourceType = sourceType;
    }

    public long getSearchProviderType() {
        return searchProviderType;
    }

    public void setSearchProviderType(long searchProviderType) {
        this.searchProviderType = searchProviderType;
    }

    public long getSearchProviderId() {
        return searchProviderId;
    }

    public void setSearchProviderId(long searchProviderId) {
        this.searchProviderId = searchProviderId;
    }

    public String getCreditUserName() {
        return creditUserName;
    }

    public void setCreditUserName(String creditUserName) {
        this.creditUserName = creditUserName;
    }

    public String getCreditUrl() {
        return creditUrl;
    }

    public void setCreditUrl(String creditUrl) {
        this.creditUrl = creditUrl;
    }

    public String getMediaDate() {
        return mediaDate;
    }

    public void setMediaDate(String mediaDate) {
        this.mediaDate = mediaDate;
    }

    public String getDataUrl() {
        return dataUrl;
    }

    public void setDataUrl(String dataUrl) {
        this.dataUrl = dataUrl;
    }

    public String getFlavorParamsIds() {
        return flavorParamsIds;
    }

    public void setFlavorParamsIds(String flavorParamsIds) {
        this.flavorParamsIds = flavorParamsIds;
    }

    public String getCategoriesIds() {
        return catoriesIds;
    }

    public void setCategoriesIds(String categoriesIds) {
        this.catoriesIds = categoriesIds;
    }

    public double getMsDuration() {
        return msDuration;
    }

    public void setMsDuration(double msDuration) {
        this.msDuration = msDuration;
    }

    @Override
    public String toString() {
        return "Result{" +
                "objectType='" + objectType + '\'' +
                ", id='" + id + '\'' +
                ", partnerId=" + partnerId +
                ", userId='" + userId + '\'' +
                ", status='" + status + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileSize='" + fileSize + '\'' +
                ", uploadedFileSize=" + uploadedFileSize +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", tags='" + tags + '\'' +
                ", adminTags='" + adminTags + '\'' +
                ", categories='" + categories + '\'' +
                ", partnerData='" + partnerData + '\'' +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", moderationStatus=" + moderationStatus +
                ", moderationCount=" + moderationCount +
                ", type=" + type +
                ", totalRank=" + totalRank +
                ", rank=" + rank +
                ", votes=" + votes +
                ", groupId=" + groupId +
                ", searchText='" + searchText + '\'' +
                ", licenseType=" + licenseType +
                ", version=" + version +
                ", thumbnailUrl='" + thumbnailUrl + '\'' +
                ", accessControlId=" + accessControlId +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", plays=" + plays +
                ", views=" + views +
                ", width=" + width +
                ", height=" + height +
                ", duration=" + duration +
                ", durationType=" + durationType +
                ", mediaType=" + mediaType +
                ", conversionQuality=" + conversionQuality +
                ", sourceType=" + sourceType +
                ", searchProviderType=" + searchProviderType +
                ", searchProviderId=" + searchProviderId +
                ", creditUserName='" + creditUserName + '\'' +
                ", creditUrl='" + creditUrl + '\'' +
                ", mediaDate='" + mediaDate + '\'' +
                ", dataUrl='" + dataUrl + '\'' +
                ", flavorParamsIds='" + flavorParamsIds + '\'' +
                ", catoriesIds='" + catoriesIds + '\'' +
                ", msDuration=" + msDuration +
                ", error=" + error +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.objectType);
        dest.writeString(this.id);
        dest.writeLong(this.partnerId);
        dest.writeString(this.userId);
        dest.writeString(this.status);
        dest.writeString(this.fileName);
        dest.writeString(this.fileSize);
        dest.writeLong(this.uploadedFileSize);
        dest.writeLong(this.createdAt);
        dest.writeLong(this.updatedAt);
        dest.writeString(this.name);
        dest.writeString(this.description);
        dest.writeString(this.tags);
        dest.writeString(this.adminTags);
        dest.writeString(this.categories);
        dest.writeString(this.partnerData);
        dest.writeString(this.downloadUrl);
        dest.writeLong(this.moderationStatus);
        dest.writeLong(this.moderationCount);
        dest.writeLong(this.type);
        dest.writeLong(this.totalRank);
        dest.writeLong(this.rank);
        dest.writeLong(this.votes);
        dest.writeLong(this.groupId);
        dest.writeString(this.searchText);
        dest.writeLong(this.licenseType);
        dest.writeLong(this.version);
        dest.writeString(this.thumbnailUrl);
        dest.writeLong(this.accessControlId);
        dest.writeLong(this.startDate);
        dest.writeLong(this.endDate);
        dest.writeLong(this.plays);
        dest.writeLong(this.views);
        dest.writeLong(this.width);
        dest.writeLong(this.height);
        dest.writeDouble(this.duration);
        dest.writeLong(this.durationType);
        dest.writeLong(this.mediaType);
        dest.writeLong(this.conversionQuality);
        dest.writeLong(this.sourceType);
        dest.writeLong(this.searchProviderType);
        dest.writeLong(this.searchProviderId);
        dest.writeString(this.creditUserName);
        dest.writeString(this.creditUrl);
        dest.writeString(this.mediaDate);
        dest.writeString(this.dataUrl);
        dest.writeString(this.flavorParamsIds);
        dest.writeString(this.catoriesIds);
        dest.writeDouble(this.msDuration);
        dest.writeParcelable(this.error, flags);
    }
}


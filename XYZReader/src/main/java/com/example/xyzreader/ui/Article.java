package com.example.xyzreader.ui;

import android.os.Parcel;
import android.os.Parcelable;

class Article implements Parcelable {
  private long id;
  private String title;
  private String publisheDate;
  private String author;
  private String thumbUrl;
  private String photoUrl;
  private String aspectRatio;
  private String body;

  public void setId(long id) {
    this.id = id;
  }

  public long getId() {
    return id;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public void setPublisheDate(String publisheDate) {
    this.publisheDate = publisheDate;
  }

  public String getPublisheDate() {
    return publisheDate;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getAuthor() {
    return author;
  }

  public void setThumbUrl(String thumbUrl) {
    this.thumbUrl = thumbUrl;
  }

  public String getThumbUrl() {
    return thumbUrl;
  }

  public void setPhotoUrl(String photoUrl) {
    this.photoUrl = photoUrl;
  }

  public String getPhotoUrl() {
    return photoUrl;
  }

  public void setAspectRatio(String aspectRatio) {
    this.aspectRatio = aspectRatio;
  }

  public String getAspectRatio() {
    return aspectRatio;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public String getBody() {
    return body;
  }


  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeLong(this.id);
    dest.writeString(this.title);
    dest.writeString(this.publisheDate);
    dest.writeString(this.author);
    dest.writeString(this.thumbUrl);
    dest.writeString(this.photoUrl);
    dest.writeString(this.aspectRatio);
    dest.writeString(this.body);
  }

  public Article() {
  }

  protected Article(Parcel in) {
    this.id = in.readLong();
    this.title = in.readString();
    this.publisheDate = in.readString();
    this.author = in.readString();
    this.thumbUrl = in.readString();
    this.photoUrl = in.readString();
    this.aspectRatio = in.readString();
    this.body = in.readString();
  }

  public static final Creator<Article> CREATOR = new Creator<Article>() {
    @Override
    public Article createFromParcel(Parcel source) {
      return new Article(source);
    }

    @Override
    public Article[] newArray(int size) {
      return new Article[size];
    }
  };
}

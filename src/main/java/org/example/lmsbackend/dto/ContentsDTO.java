package org.example.lmsbackend.dto;

public class ContentsDTO {
   private Integer contentId;
   private Integer moduleId;
   private String title;
   private String type; // video, document, text, quiz
   private String contentUrl;
   private Integer orderNumber;

   // Getters and Setters
   public Integer getContentId() {
      return contentId;
   }

   public void setContentId(Integer contentId) {
      this.contentId = contentId;
   }

   public Integer getModuleId() {
      return moduleId;
   }

   public void setModuleId(Integer moduleId) {
      this.moduleId = moduleId;
   }

   public String getTitle() {
      return title;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public String getType() {
      return type;
   }

   public void setType(String type) {
      this.type = type;
   }

   public String getContentUrl() {
      return contentUrl;
   }

   public void setContentUrl(String contentUrl) {
      this.contentUrl = contentUrl;
   }

   public Integer getOrderNumber() {
      return orderNumber;
   }

   public void setOrderNumber(Integer orderNumber) {
      this.orderNumber = orderNumber;
   }
}

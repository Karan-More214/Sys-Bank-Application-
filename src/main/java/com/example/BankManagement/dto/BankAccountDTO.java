package com.example.BankManagement.dto;

import com.example.BankManagement.Entity.BankEntity;

public class BankAccountDTO {

    private Integer id;
    private String firstname;
    private String lastname;
    private String email;
    private String phoneNo;
    private String address;
    private String dob;
    private String gender;
    private String accountType;
    private String accountStatus;
    private String kycStatus;
    private String kycRejectionReason;
    private Double balance;
    private String createdAt;
    private String photoUrl;
    private String aadhaarUrl;
    private String panUrl;

    private String jointFullName;
    private String jointHolderId;
    private String jointEmail;
    private String jointPhoneNo;
    private String jointAddress;
    private String jointDob;
    private String jointGender;
    private String jointPhotoUrl;
    private String jointAadhaarUrl;
    private String jointPanUrl;

    public BankAccountDTO() {
    }

    public static BankAccountDTO fromEntity(BankEntity account) {
        if (account == null) return null;
        BankAccountDTO dto = new BankAccountDTO();
        dto.setId(account.getId());
        dto.setFirstname(account.getFirstname());
        dto.setLastname(account.getLastname());
        dto.setEmail(account.getEmail());
        dto.setPhoneNo(account.getPhoneNo());
        dto.setAddress(account.getAddress());
        dto.setDob(account.getDob());
        dto.setGender(account.getGender());
        dto.setAccountType(account.getAccountType());
        dto.setAccountStatus(account.getAccountStatus());
        dto.setKycStatus(account.getKycStatus());
        dto.setKycRejectionReason(account.getKycRejectionReason());
        dto.setBalance(account.getBalance());
        if (account.getCreatedAt() != null) {
            dto.setCreatedAt(account.getCreatedAt().toString());
        }
        if (account.getProfilePhoto() != null) {
            dto.setPhotoUrl("/bank/user/profile/photo/" + account.getId());
        }
        if (account.getAadhaar() != null) {
            dto.setAadhaarUrl("/bank/user/profile/aadhaar/" + account.getId());
        }
        if (account.getPan() != null) {
            dto.setPanUrl("/bank/user/profile/pan/" + account.getId());
        }

        if (account.isJointAccount()) {
            dto.setJointFullName(account.getJointFullName());
            dto.setJointHolderId(account.getJointHolderId());
            dto.setJointEmail(account.getJointEmail());
            dto.setJointPhoneNo(account.getJointPhoneNo());
            dto.setJointAddress(account.getJointAddress());
            dto.setJointDob(account.getJointDob());
            dto.setJointGender(account.getJointGender());
            if (account.getJointProfilePhoto() != null) {
                dto.setJointPhotoUrl("/bank/user/joint/photo/" + account.getId());
            }
            if (account.getJointAadhaarFile() != null) {
                dto.setJointAadhaarUrl("/bank/user/joint-aadhaar/" + account.getId());
            }
            if (account.getJointPanFile() != null) {
                dto.setJointPanUrl("/bank/user/joint-pan/" + account.getId());
            }
        }

        return dto;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getFirstname() { return firstname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }

    public String getLastname() { return lastname; }
    public void setLastname(String lastname) { this.lastname = lastname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNo() { return phoneNo; }
    public void setPhoneNo(String phoneNo) { this.phoneNo = phoneNo; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

    public String getKycStatus() { return kycStatus; }
    public void setKycStatus(String kycStatus) { this.kycStatus = kycStatus; }

    public String getKycRejectionReason() { return kycRejectionReason; }
    public void setKycRejectionReason(String kycRejectionReason) { this.kycRejectionReason = kycRejectionReason; }

    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getAadhaarUrl() { return aadhaarUrl; }
    public void setAadhaarUrl(String aadhaarUrl) { this.aadhaarUrl = aadhaarUrl; }

    public String getPanUrl() { return panUrl; }
    public void setPanUrl(String panUrl) { this.panUrl = panUrl; }

    public String getJointFullName() { return jointFullName; }
    public void setJointFullName(String jointFullName) { this.jointFullName = jointFullName; }

    public String getJointHolderId() { return jointHolderId; }
    public void setJointHolderId(String jointHolderId) { this.jointHolderId = jointHolderId; }

    public String getJointEmail() { return jointEmail; }
    public void setJointEmail(String jointEmail) { this.jointEmail = jointEmail; }

    public String getJointPhoneNo() { return jointPhoneNo; }
    public void setJointPhoneNo(String jointPhoneNo) { this.jointPhoneNo = jointPhoneNo; }

    public String getJointAddress() { return jointAddress; }
    public void setJointAddress(String jointAddress) { this.jointAddress = jointAddress; }

    public String getJointDob() { return jointDob; }
    public void setJointDob(String jointDob) { this.jointDob = jointDob; }

    public String getJointGender() { return jointGender; }
    public void setJointGender(String jointGender) { this.jointGender = jointGender; }

    public String getJointPhotoUrl() { return jointPhotoUrl; }
    public void setJointPhotoUrl(String jointPhotoUrl) { this.jointPhotoUrl = jointPhotoUrl; }

    public String getJointAadhaarUrl() { return jointAadhaarUrl; }
    public void setJointAadhaarUrl(String jointAadhaarUrl) { this.jointAadhaarUrl = jointAadhaarUrl; }

    public String getJointPanUrl() { return jointPanUrl; }
    public void setJointPanUrl(String jointPanUrl) { this.jointPanUrl = jointPanUrl; }
}

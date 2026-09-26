package com.example.BankManagement.Entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bank_accounts")
public class BankEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ---------- Primary Account Holder ----------
    @Column(nullable = false, length = 50)
    private String firstname;

    @Column(nullable = false, length = 50)
    private String lastname;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "phone_no", length = 15)
    private String phoneNo;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "dob")
    private String dob;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(nullable = false, columnDefinition = "DECIMAL(15,2) DEFAULT 0.00")
    private Double balance = 0.0;

    @Column(name = "account_type", length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'SAVINGS'")
    private String accountType = "SAVINGS";

    @Column(name = "account_status", length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'ACTIVE'")
    private String accountStatus = "ACTIVE";

    // KYC document review status. Defaults to VERIFIED (not PENDING) so adding this column
    // never retroactively blocks every account that already existed before this feature
    // shipped - only BankController.submitAccount explicitly sets PENDING, for genuinely
    // new accounts going through document upload for the first time.
    @Column(name = "kyc_status", length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'VERIFIED'")
    private String kycStatus = "VERIFIED";

    @Column(name = "kyc_rejection_reason", length = 500)
    private String kycRejectionReason;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    // ---------- Primary Holder Documents ----------
    @Lob
    @Column(name = "profile_photo", columnDefinition = "LONGBLOB")
    private byte[] profilePhoto;

    @Lob
    @Column(name = "aadhaar_file", columnDefinition = "LONGBLOB")
    private byte[] aadhaar;

    @Lob
    @Column(name = "pan_file", columnDefinition = "LONGBLOB")
    private byte[] pan;

    // ---------- Joint Account Holder ----------
    @Column(name = "joint_first_name", length = 50)
    private String jointFirstName;

    @Column(name = "joint_last_name", length = 50)
    private String jointLastName;

    @Column(name = "joint_email", length = 100)
    private String jointEmail;

    @Column(name = "joint_phone_no", length = 15)
    private String jointPhoneNo;

    @Column(name = "joint_address", columnDefinition = "TEXT")
    private String jointAddress;

    @Column(name = "joint_dob", length = 10)
    private String jointDob;

    // ✅ Added missing joint holder gender field
    @Column(name = "joint_gender", length = 10)
    private String jointGender;

    // ✅ Added joint holder ID for identification
    @Column(name = "joint_holder_id")
    private String jointHolderId;

    // ---------- Joint Holder Documents ----------
    @Lob
    @Column(name = "joint_profile_photo", columnDefinition = "LONGBLOB")
    private byte[] jointProfilePhoto;

    @Lob
    @Column(name = "joint_aadhaar_file", columnDefinition = "LONGBLOB")
    private byte[] jointAadhaarFile;

    @Lob
    @Column(name = "joint_pan_file", columnDefinition = "LONGBLOB")
    private byte[] jointPanFile;

    // ---------- Constructors ----------
    public BankEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public BankEntity(Integer id, String firstname, String lastname, String email, String phoneNo, String gender,
                      String dob, String address, Double balance, String accountType, String accountStatus,
                      LocalDateTime createdAt, LocalDateTime updatedAt, byte[] profilePhoto, byte[] aadhaar, byte[] pan,
                      String jointFirstName, String jointLastName, String jointEmail, String jointPhoneNo,
                      String jointAddress, String jointDob, String jointGender, String jointHolderId,
                      byte[] jointProfilePhoto, byte[] jointAadhaarFile, byte[] jointPanFile) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.phoneNo = phoneNo;
        this.gender = gender;
        this.dob = dob;
        this.address = address;
        this.balance = balance;
        this.accountType = accountType;
        this.accountStatus = accountStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.profilePhoto = profilePhoto;
        this.aadhaar = aadhaar;
        this.pan = pan;
        this.jointFirstName = jointFirstName;
        this.jointLastName = jointLastName;
        this.jointEmail = jointEmail;
        this.jointPhoneNo = jointPhoneNo;
        this.jointAddress = jointAddress;
        this.jointDob = jointDob;
        this.jointGender = jointGender;
        this.jointHolderId = jointHolderId;
        this.jointProfilePhoto = jointProfilePhoto;
        this.jointAadhaarFile = jointAadhaarFile;
        this.jointPanFile = jointPanFile;
    }

    // ---------- JPA Lifecycle ----------
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.accountType == null || this.accountType.trim().isEmpty()) this.accountType = "SAVINGS";
        if (this.accountStatus == null || this.accountStatus.trim().isEmpty()) this.accountStatus = "ACTIVE";
        if (this.balance == null) this.balance = 0.0;
        
        // ✅ Auto-generate joint holder ID if it's a joint account
        if ("joint".equalsIgnoreCase(this.accountType) && this.jointHolderId == null) {
            this.jointHolderId = "JNT" + System.currentTimeMillis();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ---------- Utility Methods ----------
    public String getFullName() {
        return (firstname != null ? firstname : "") + " " + (lastname != null ? lastname : "");
    }

    public String getJointFullName() {
        if (jointFirstName == null && jointLastName == null) return null;
        return (jointFirstName != null ? jointFirstName : "") + " " + (jointLastName != null ? jointLastName : "");
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(accountStatus);
    }

    public boolean hasSufficientBalance(double amount) {
        return getBalance() >= amount;
    }

    public String getFormattedBalance() {
        return String.format("₹%.2f", getBalance());
    }

    // ✅ Fixed: Better joint account check
    public boolean isJointAccount() {
        return "joint".equalsIgnoreCase(accountType) && 
               jointFirstName != null && !jointFirstName.trim().isEmpty();
    }

    // ✅ Added: Check if joint holder data exists
    public boolean hasJointHolderData() {
        return jointFirstName != null && !jointFirstName.trim().isEmpty() &&
               jointLastName != null && !jointLastName.trim().isEmpty();
    }

    // ---------- Getters & Setters ----------
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

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Double getBalance() { return balance != null ? balance : 0.0; }
    public void setBalance(Double balance) { this.balance = balance != null ? balance : 0.0; }

    // ✅ Fixed: Return consistent case for account type
    public String getAccountType() { 
        return accountType != null ? accountType.toLowerCase() : "savings"; 
    }
    public void setAccountType(String accountType) { 
        this.accountType = accountType != null ? accountType.toLowerCase() : "savings"; 
    }

    public String getAccountStatus() { return accountStatus != null ? accountStatus.toUpperCase() : "ACTIVE"; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

    public String getKycStatus() { return kycStatus != null ? kycStatus.toUpperCase() : "VERIFIED"; }
    public void setKycStatus(String kycStatus) { this.kycStatus = kycStatus; }

    public String getKycRejectionReason() { return kycRejectionReason; }
    public void setKycRejectionReason(String kycRejectionReason) { this.kycRejectionReason = kycRejectionReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public byte[] getProfilePhoto() { return profilePhoto; }
    public void setProfilePhoto(byte[] profilePhoto) { this.profilePhoto = profilePhoto; }

    public byte[] getAadhaar() { return aadhaar; }
    public void setAadhaar(byte[] aadhaar) { this.aadhaar = aadhaar; }

    public byte[] getPan() { return pan; }
    public void setPan(byte[] pan) { this.pan = pan; }

    public String getJointFirstName() { return jointFirstName; }
    public void setJointFirstName(String jointFirstName) { this.jointFirstName = jointFirstName; }

    public String getJointLastName() { return jointLastName; }
    public void setJointLastName(String jointLastName) { this.jointLastName = jointLastName; }

    public String getJointEmail() { return jointEmail; }
    public void setJointEmail(String jointEmail) { this.jointEmail = jointEmail; }

    public String getJointPhoneNo() { return jointPhoneNo; }
    public void setJointPhoneNo(String jointPhoneNo) { this.jointPhoneNo = jointPhoneNo; }

    public String getJointAddress() { return jointAddress; }
    public void setJointAddress(String jointAddress) { this.jointAddress = jointAddress; }

    public String getJointDob() { return jointDob; }
    public void setJointDob(String jointDob) { this.jointDob = jointDob; }

    // ✅ Added: Joint gender getter/setter
    public String getJointGender() { return jointGender; }
    public void setJointGender(String jointGender) { this.jointGender = jointGender; }

    // ✅ Added: Joint holder ID getter/setter
    public String getJointHolderId() { return jointHolderId; }
    public void setJointHolderId(String jointHolderId) { this.jointHolderId = jointHolderId; }

    public byte[] getJointProfilePhoto() { return jointProfilePhoto; }
    public void setJointProfilePhoto(byte[] jointProfilePhoto) { this.jointProfilePhoto = jointProfilePhoto; }

    public byte[] getJointAadhaarFile() { return jointAadhaarFile; }
    public void setJointAadhaarFile(byte[] jointAadhaarFile) { this.jointAadhaarFile = jointAadhaarFile; }

    public byte[] getJointPanFile() { return jointPanFile; }
    public void setJointPanFile(byte[] jointPanFile) { this.jointPanFile = jointPanFile; }

    // ✅ Deprecated: Keep these for backward compatibility
    @Deprecated
    public byte[] getJointAadhaar() { return jointAadhaarFile; }
    @Deprecated
    public void setJointAadhaar(byte[] jointAadhaar) { this.jointAadhaarFile = jointAadhaar; }
    @Deprecated
    public byte[] getJointPan() { return jointPanFile; }
    @Deprecated
    public void setJointPan(byte[] jointPan) { this.jointPanFile = jointPan; }

    // ---------- toString ----------
    @Override
    public String toString() {
        return "BankEntity{" +
                "id=" + id +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", email='" + email + '\'' +
                ", jointFirstName='" + jointFirstName + '\'' +
                ", jointLastName='" + jointLastName + '\'' +
                ", jointEmail='" + jointEmail + '\'' +
                ", accountType='" + accountType + '\'' +
                ", balance=" + balance +
                '}';
    }
}
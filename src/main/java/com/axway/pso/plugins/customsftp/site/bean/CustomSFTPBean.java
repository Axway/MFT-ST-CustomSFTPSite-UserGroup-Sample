package com.axway.pso.plugins.customsftp.site.bean;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.axway.st.plugins.site.Encrypted;
import com.axway.st.plugins.site.UIBean;
import com.axway.st.plugins.site.services.CertificateService;

public class CustomSFTPBean implements UIBean {
	 /** Holds the partner hostname. */
    @NotNull(message="Host could not be null")
    @Size(min=1, message="Host could not be empty")
    @Pattern(regexp="\\S*", message="Host could not contain whitespaces.")
    private String msftpHost;

    /** Holds the partner port. */
    @NotNull(message="Port could not be null")
    @Min(value=0, message="Port should be not negative integer.")
    @Max(value=65535, message="Invalid port number. It should be less or equal to 65535.")
    private Integer msftpPort;

    /** Holds the login name. */
    @NotNull(message="Username could not be null")
    @Size(min=1, message="Username could not be empty")
    @Pattern(regexp="\\S*", message="Username could not contain whitespaces.")
    private String msftpUserName;

    /** The user's password. */
    private String msftpPassword;

	private boolean mCsftp_usePassword_Control;

	private String mClientLocalCertificate;
	private boolean mCsftpUploadAllowOverwrite;
    

	  private CertificateService mCertificateService;
	  
    /** The remote folder to download files from. */
    private String msftpDownloadFolder;

    /** The download pattern. */
    private String msftpDownloadPattern;

    /** The remote folder to push files to. */
    private String msftpUploadFolder;

    /** Holds the network zone name. */
    private String mNetworkZone;

    private String csftp_networkZone;
    
    private boolean csftpDownloadFolderAdvancedExpressionEnabled;
    private boolean csftpDownloadPatternAdvancedExpressionEnabled;
    private String csftpPatternType;
    
    /** Advanced settings */

    private String csftpCipherSuites="aes256-ctr";
    private String csftpHmacs="hmac-sha2-256";
    private String csftpKex="ecdh-sha2-nistp256";


    /**
     * custom command to execute during upload.
     */
    private String msftpCustomCmdUpd;

    /**
     * custom command to execute during Download.
     */
    private String msftpCustomCmdDwn;
    /** Gets the the hostname of a remote partner.
     * 
     * @return the remote partner hostname
     */
    public String getCsftpHost() {
        return msftpHost;
    }

    /** Updates the remote partner host.
     * 
     * @param csftpHost the remote partner host.
     */
    public void setCsftpHost(String csftpHost) {
    	msftpHost=csftpHost;
    }
    
    /** Returns the port, used to connect to a remote partner.
     * 
     * @return the port, used to connect to a remote partner.
     */
    public Integer getCsftpPort() {
        return msftpPort;
    }

    /** Changes the port.
     * 
     * @param csftpPort new port value
     */
    public void setCsftpPort(Integer csftpPort) {
    	msftpPort=csftpPort ;
    }

    /** The user name to login.
     *  
     * @return the user name
     */
    public String getCsftpUserName() {
        return msftpUserName;
    }

    /** Sets the partner user name.
     * 
     * @param csftpUserName the login name to the remote partner
     */
    public void setCsftpUserName(String csftpUserName) {
        msftpUserName=csftpUserName ;
    }
    
    /** The user's password.
     * 
     * @return the passowrd
     */
    @Encrypted
    public String getCsftpPassword() {
        return msftpPassword;
    }

    /** Sets the user's password.
     * 
     * @param csftpPassword the user's password.
     */
    @Encrypted
    public void setCsftpPassword(String csftpPassword) {
        msftpPassword = csftpPassword;
    }
    
    /** Returns the remote download folder.
     * 
     * @return the remote download folder.
     */
    public String getCsftpDownloadFolder() {
        return msftpDownloadFolder;
    }

    /** Sets the remote download folder.
     * 
     * @param downloadFolder the remote download folder.
     */
    public void setCsftpDownloadFolder(String csftpDownloadFolder) {
        msftpDownloadFolder = csftpDownloadFolder;
    }
    
    /** Returns the download partner.
     * 
     * @return the download partner.
     */
    public String getCsftpDownloadPattern() {
        return msftpDownloadPattern;
    }

    /** Sets the download partner.
     * 
     * @param csftpDownloadPattern the download partner.
     */
    public void setCsftpDownloadPattern(String csftpDownloadPattern) {
        msftpDownloadPattern = csftpDownloadPattern;
    }

    /** Returns the remote upload folder.
     * 
     * @return the remote upload folder.
     */
    public String getCsftpUploadFolder() {
        return msftpUploadFolder;
    }

    /** Sets the remote upload folder.
     * 
     * @param csftpUploadFolder the upload folder
     */
    public void setCsftpUploadFolder(String csftpUploadFolder) {
       msftpUploadFolder = csftpUploadFolder;
    }

    /** Returns the network zone.
     * 
     * @return the network zone.
     */
    public String getNetworkZone() {
        return mNetworkZone;
    }

    /** Sets the network zone.
     * 
     * @param networkZone the network zone.
     */
    public void setNetworkZone(String networkZone) {
        mNetworkZone = networkZone;
    }

    /**
     * Get the Custom Command string. 
     * @return the command to execute
     */
    public String getCsftpCustomCmdUpd() {
        return msftpCustomCmdUpd;
    }
    
    /**
     * Set the Custom command to execute. 
     * @param csftpCustomCmdUpd the command to execute
     */
    public void setCsftpCustomCmdUpd(String csftpCustomCmdUpd){
        msftpCustomCmdUpd = csftpCustomCmdUpd;
    }

    /**
     * Get the Custom Command string. 
     * @return the command to execute
     */
    public String getCsftpCustomCmdDwn() {
        return msftpCustomCmdDwn;
    }
    
    /**
     * Set the Custom command to execute. 
     * @param csftpCustomCmdUpd the command to execute
     */
    public void setCsftpCustomCmdDwn(String csftpCustomCmdDwn){
        msftpCustomCmdDwn = csftpCustomCmdDwn;
    }

	public boolean isCsftpDownloadFolderAdvancedExpressionEnabled() {
		return csftpDownloadFolderAdvancedExpressionEnabled;
	}

	public void setCsftpDownloadFolderAdvancedExpressionEnabled(boolean csftpDownloadFolderAdvancedExpressionEnabled) {
		this.csftpDownloadFolderAdvancedExpressionEnabled = csftpDownloadFolderAdvancedExpressionEnabled;
	}

	public boolean isCsftpDownloadPatternAdvancedExpressionEnabled() {
		return csftpDownloadPatternAdvancedExpressionEnabled;
	}

	public void setCsftpDownloadPatternAdvancedExpressionEnabled(boolean csftpDownloadPatternAdvancedExpressionEnabled) {
		this.csftpDownloadPatternAdvancedExpressionEnabled = csftpDownloadPatternAdvancedExpressionEnabled;
	}

	public String getCsftpPatternType() {
		return csftpPatternType;
	}

	public void setCsftpPatternType(String csftpPatternType) {
		this.csftpPatternType = csftpPatternType;
	}

	public boolean isCsftp_usePassword_Control() {
		return mCsftp_usePassword_Control;
	}



	public void setCsftp_usePassword_Control(boolean csftp_usePassword_Control) {
		this.mCsftp_usePassword_Control = csftp_usePassword_Control;
	}


	public String getClientLocalCertificate() { 
		return this.mClientLocalCertificate; 
		}

	public void setClientLocalCertificate(String clientLocalCertificate) { 
		this.mClientLocalCertificate = clientLocalCertificate; 
		}

	public boolean isCsftpUploadAllowOverwrite() {
		return mCsftpUploadAllowOverwrite;
	}

	public void setCsftpUploadAllowOverwrite(boolean csftpUploadAllowOverwrite) {
		this.mCsftpUploadAllowOverwrite = csftpUploadAllowOverwrite;
	}

	public CertificateService getCertificateService() {
		return this.mCertificateService;
	}
//Cipher changes start
	
	public String getCsftpCipherSuites() {
		return csftpCipherSuites;
	}

	public void setCsftpCipherSuites(String csftpCipherSuites) {
		this.csftpCipherSuites = csftpCipherSuites;
	}

	public String getCsftpHmacs() {
		return csftpHmacs;
	}

	public void setCsftpHmacs(String csftpHmacs) {
		this.csftpHmacs = csftpHmacs;
	}

	public String getCsftpKex() {
		return csftpKex;
	}

	public void setCsftpKex(String csftpKex) {
		this.csftpKex = csftpKex;
	}


	public String getCsftp_networkZone() {
		return csftp_networkZone;
	}

	public void setCsftp_networkZone(String csftp_networkZone) {
		this.csftp_networkZone = csftp_networkZone;
	}


	
	
	//Cipher changes end
	

}


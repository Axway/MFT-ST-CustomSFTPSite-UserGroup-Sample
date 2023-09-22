package com.axway.pso.plugins.customsftp.site;

import com.axway.pso.plugins.customsftp.site.bean.CustomSFTPBean;
import com.axway.st.plugins.site.CustomSite;
import com.axway.st.plugins.site.DestinationFile;
import com.axway.st.plugins.site.FileItem;
import com.axway.st.plugins.site.FlowAttributesData;
import com.axway.st.plugins.site.RemotePartner;
import com.axway.st.plugins.site.SourceFile;
import com.axway.st.plugins.site.TransferFailedException;
import com.axway.st.plugins.site.UIBean;
import com.axway.st.plugins.site.expression.InvalidExpressionException;
import com.axway.st.plugins.site.services.AdditionalInfoLoggingService;
import com.axway.st.plugins.site.services.CertificateService;
import com.axway.st.plugins.site.services.CommandLoggingService;
import com.axway.st.plugins.site.services.ExpressionEvaluatorService;
import com.axway.st.plugins.site.services.LoggingService;
import com.axway.st.plugins.site.services.ProxyService;
import com.axway.st.plugins.site.services.proxy.ProxyInfo;
import com.axway.st.plugins.site.services.proxy.ProxyType;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Proxy;
import com.jcraft.jsch.ProxySOCKS5;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import com.tumbleweed.util.StringUtil;
import com.tumbleweed.util.expressions.ExpressionException;
import com.tumbleweed.util.expressions.InvalidSyntaxException;
import java.io.IOException;
import java.io.StringWriter;
import java.net.Authenticator;
import java.net.InetAddress;
import java.net.PasswordAuthentication;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.inject.Inject;
import org.apache.oro.text.GlobCompiler;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemObjectGenerator;
import org.bouncycastle.util.io.pem.PemWriter;

public class CustomSFTPSite extends CustomSite {
  private CustomSFTPBean mSFTPBean = new CustomSFTPBean();
  
  private JSch jsch;
  
  private Session session;

  @Inject
  private ProxyService mProxyService;
  
  private ProxyInfo mProxyInfo;
  
  @Inject
  private CommandLoggingService mCommandLoggingService;
  
  @Inject
  private AdditionalInfoLoggingService mAdditionalInfoLogService;
  
  @Inject
  private CertificateService mCertificateService;
  
  @Inject
  private LoggingService log;
  
  @Inject
  private FlowAttributesData mFlowAttributesData;
  
  @Inject
  private ExpressionEvaluatorService mExpressionEvaluatorService;
  
  public CustomSFTPSite() {
    setUIBean((UIBean)this.mSFTPBean);
  }
  
  public void connect() throws TransferFailedException {
    this.log.info("Start connecting with the custom protocol connector");
    try {
    	String kex, cipher, hmac;
    	
      setProxyService(this.mProxyService);
      resolveExpressionFields(this.mSFTPBean);
      
      
      if(!StringUtil.isNullOrEmpty(this.mSFTPBean.getCsftpKex())) {
    	  kex=this.mSFTPBean.getCsftpKex().replaceAll(" ","");
      }
      else {
    	  	kex=JSch.getConfig("kex") ;
      }
      
      
      if(!StringUtil.isNullOrEmpty(this.mSFTPBean.getCsftpCipherSuites())) {
      cipher=this.mSFTPBean.getCsftpCipherSuites().replaceAll(" ","");
      }else {
    	  cipher=JSch.getConfig("cipher.c2s") ;
      }
      
      
      if(!StringUtil.isNullOrEmpty(this.mSFTPBean.getCsftpHmacs())) {
      hmac=this.mSFTPBean.getCsftpHmacs().replaceAll(" ", "");
      }else {
    	  hmac=JSch.getConfig("mac.c2s") ;
      }
      
      this.log.info("Configured kex: "+kex);
      this.log.info("Configured cipher: "+cipher);
      this.log.info("Configured hmac: "+hmac);
      
      JSch.setConfig("kex",kex);
      JSch.setConfig("cipher.s2c",cipher);
      JSch.setConfig("cipher.c2s",cipher);
      JSch.setConfig("mac.c2s",hmac);
      JSch.setConfig("mac.s2c",hmac);
      
      this.jsch = new JSch();
      this.session = this.jsch.getSession(this.mSFTPBean.getCsftpUserName(), this.mSFTPBean.getCsftpHost(), this.mSFTPBean.getCsftpPort().intValue());
      if (this.mSFTPBean.isCsftp_usePassword_Control() == true) {
        this.session.setPassword(this.mSFTPBean.getCsftpPassword());
      } else if (this.mSFTPBean.getClientLocalCertificate() != null) {
        String certId = this.mSFTPBean.getClientLocalCertificate();
        this.session.setConfig("PreferredAuthentications", "publickey");
        byte[] encodedPrivateKey = this.mCertificateService.getKeyPair(certId).getPrivate().getEncoded();
        PrivateKeyInfo pkInfo = PrivateKeyInfo.getInstance(encodedPrivateKey);
        ASN1Encodable privateKeyPKCS1ASN1Encodable = pkInfo.parsePrivateKey();
        ASN1Primitive privateKeyPKCS1ASN1 = privateKeyPKCS1ASN1Encodable.toASN1Primitive();
        byte[] privateKeyPKCS1 = privateKeyPKCS1ASN1.getEncoded();
        PemObject pemObject = new PemObject("RSA PRIVATE KEY", privateKeyPKCS1);
        StringWriter stringWriter = new StringWriter();
        PemWriter pemWriter = new PemWriter(stringWriter);
        pemWriter.writeObject((PemObjectGenerator)pemObject);
        pemWriter.flush();
        pemWriter.close();
        String pemString = stringWriter.toString();
        this.jsch.addIdentity(this.mSFTPBean.getCsftpUserName(), pemString.getBytes(), null, null);
      } else {
        this.log.error("Error obtaining Site Login Credentials. Please select either certificate authentication or Password authentication");
        this.session.disconnect();
      } 
      if (this.session != null) {
        this.session.setConfig("StrictHostKeyChecking", "no");
        this.session.connect();
        this.log.info("Custom SFTP session Connected Successfuly");
      } else {
        this.log.error("Error occurrred while creating a session as the session object is null");
      } 
    } catch (Exception iee) {
    	String message="An error occurred while creating a connection to the remote site: " + iee.getMessage();
      this.log.error(message);
      throw new TransferFailedException(message, iee, true);
    } 
  }
  
  public void finalizeExecution() {
    try {
      if (this.session != null) {
        this.log.info("Disconnect from client.");
        this.session.disconnect();
        this.session = null;
        this.jsch.removeAllIdentity();
      } 
    } catch (JSchException e) {
      this.log.error("Exception occurred during session disconnect : " + e.getMessage());
    } 
  }
  
  public void getFile(DestinationFile file) throws IOException {
    this.log.info(MessageFormat.format("Getting file {0}.", new Object[] { file.getName() }));
    try {
      this.log.info(MessageFormat.format("Downloading file {0}.", new Object[] { file.getName() }));
      String downFolder = this.mSFTPBean.getCsftpDownloadFolder();
      this.log.info("DownFolder is - " + downFolder);
      String execCmd = this.mSFTPBean.getCsftpCustomCmdDwn();

      connect();
      RemotePartner descriptor = new RemotePartner(this.mSFTPBean.getCsftpHost(), this.mSFTPBean.getCsftpDownloadFolder());
      ChannelSftp cs = (ChannelSftp)this.session.openChannel("sftp");
      cs.connect();
      

      this.log.info("executing command ls " + execCmd);
      String output = cs.ls(execCmd).toString();
      this.log.info("output of download ls " + execCmd + " cmd is - " + output);
      this.log.info("executing file download");
      cs.get(downFolder + "/" + file.getName(), file.getOutputStream(descriptor));
      this.log.info(MessageFormat.format("File {0} Downloaded Successfuly", new Object[] { file.getName() }));
      cs.disconnect();
    } catch (Exception e) {
    	String message=MessageFormat.format("Error downloading  file {0} - Exception - {1}", new Object[] { file.getName(), e.getMessage() });
      this.log.error(message);
      throw new TransferFailedException(message, e, true);
    } 
  }
  
  public List<FileItem> list() throws IOException {
    this.log.info("Performing list operation ...");
    try {
      connect();
      String csftpDownloadFolder = this.mSFTPBean.getCsftpDownloadFolder();
      List<FileItem> list = new ArrayList<>();
      boolean isdownloadPatternAdvancedExpressionEnabled = this.mSFTPBean.isCsftpDownloadPatternAdvancedExpressionEnabled();
      String remotePattern = isdownloadPatternAdvancedExpressionEnabled ? getExpressionEvaluatedValue(this.mSFTPBean.getCsftpDownloadPattern()) : this.mSFTPBean.getCsftpDownloadPattern();
      this.log.info("Download pattern is: " + remotePattern);
      boolean isValidRemotePattern = !StringUtil.isNullOrEmpty(remotePattern);
      Perl5Matcher perl5Matcher = new Perl5Matcher();
      Pattern pattern = null;
      ChannelSftp channelL = (ChannelSftp)this.session.openChannel("sftp");
      channelL.connect();
      Vector<ChannelSftp.LsEntry> vector = channelL.ls(csftpDownloadFolder);
      String patternType = this.mSFTPBean.getCsftpPatternType();
      try {
        if (isValidRemotePattern)
          if ("regex".equalsIgnoreCase(patternType)) {
            pattern = (new Perl5Compiler()).compile(remotePattern);
          } else {
            pattern = (new GlobCompiler()).compile(remotePattern);
          }  
      } catch (MalformedPatternException e) {
        this.log.error("Error compiling download pattern: " + e.getMessage(), (Throwable)e);
        throw new TransferFailedException("Error compiling download pattern: " + e.getMessage(), e);
      } 
      for (ChannelSftp.LsEntry entry : vector) {
        if (isValidRemotePattern && perl5Matcher
          .matches(entry.getFilename().toString(), pattern)) {
          FileItem item = new FileItem(entry.getFilename().toString());
          list.add(item);
        } 
      } 
      if (list.size() == 0)
        this.log.info("No files matched by pattern: " + remotePattern); 
      channelL.disconnect();
      return list;
    } catch (SftpException|JSchException|SecurityException | InvalidSyntaxException e) {
      throw new TransferFailedException("Cannot execute ls command on sftp connection", e);
      
    } 
  }
  
  public void putFile(SourceFile file) throws IOException {
    try {
      this.log.info(MessageFormat.format("Sending file {0} with size {1}.", new Object[] { file.getName(), Long.valueOf(file.getSize()) }));
      String upFolder = this.mSFTPBean.getCsftpUploadFolder();
      String execCmd = this.mSFTPBean.getCsftpCustomCmdUpd();
    
      connect();
      RemotePartner descriptor = new RemotePartner(this.mSFTPBean.getCsftpHost(), this.mSFTPBean.getCsftpUploadFolder());
      ChannelSftp cs = (ChannelSftp)this.session.openChannel("sftp");
      cs.connect();
 
      this.log.info("executing command ls " + execCmd);
      String output = cs.ls(execCmd).toString();
      this.log.info("output of upload ls " + execCmd + " cmd is - " + output);

      this.log.info("Executing upload file command");
      cs.put(file.getInputStream(descriptor), upFolder + "/" + file.getName());
      this.log.info(MessageFormat.format("File {0} Uploaded Successfuly", new Object[] { file.getName() }));
      cs.disconnect();
    } catch (Exception e) {
    	String message=MessageFormat.format("Exception occurred while uploading the file {0} -", new Object[] { file.getName() })+e.getMessage();
      this.log.error(message);
      throw new TransferFailedException(message, e, true);
    } 
  }
  
  private void resolveExpressionFields(CustomSFTPBean customSFTPBean) throws Exception  {
    this.mExpressionEvaluatorService.loadExpressionService(getFlowAttributesData().getFlowAttributes());
    String csftpDownloadFolder = customSFTPBean.isCsftpDownloadFolderAdvancedExpressionEnabled() ? getExpressionEvaluatedValue(customSFTPBean.getCsftpDownloadFolder()) : customSFTPBean.getCsftpDownloadFolder();
    if (csftpDownloadFolder != null)
      customSFTPBean.setCsftpDownloadFolder(this.mExpressionEvaluatorService.evaluatePathString(csftpDownloadFolder)); 
    String csftpDownloadPattern = customSFTPBean.isCsftpDownloadPatternAdvancedExpressionEnabled() ? getExpressionEvaluatedValue(customSFTPBean.getCsftpDownloadPattern()) : customSFTPBean.getCsftpDownloadPattern();
    if (csftpDownloadPattern != null)
      customSFTPBean.setCsftpDownloadPattern(this.mExpressionEvaluatorService.evaluateString(csftpDownloadPattern)); 
    String csftpUploadFolder_Route = this.mExpressionEvaluatorService.evaluateString("${uploadFolderOverwrite}");
  //  this.log.info("Upload folder overwrite value: " + csftpUploadFolder_Route);
    if (customSFTPBean.isCsftpUploadAllowOverwrite()) {
      if (!StringUtil.isNullOrEmpty(csftpUploadFolder_Route)) {
        customSFTPBean.setCsftpUploadFolder(this.mExpressionEvaluatorService.evaluatePathString(csftpUploadFolder_Route));
      } else {
        customSFTPBean.setCsftpUploadFolder(customSFTPBean.getCsftpUploadFolder());
      } 
    } else {
      customSFTPBean.setCsftpUploadFolder(customSFTPBean.getCsftpUploadFolder());
    } 
    String csftpUserName = customSFTPBean.getCsftpUserName();
    customSFTPBean.setCsftpUserName(this.mExpressionEvaluatorService.evaluateString(csftpUserName));
  }
  
  private String getExpressionEvaluatedValue(String expression) throws IOException, InvalidSyntaxException {
    String evaluatedValue = null;
    try {
      evaluatedValue = this.mExpressionEvaluatorService.evaluateString(expression);
    } catch (Exception e) {
      String errorMsg = "Could not evaluate value from expression: " + expression;
      this.log.error(errorMsg, (Throwable)e);
      throw new TransferFailedException(errorMsg, e,true);
    } 
    return evaluatedValue;
  }
  
  public List<String> getProtocolCommands() {
    return this.mCommandLoggingService.getProtocolCommands();
  }
  
  public String getAdditionalInfo() {
    return this.mAdditionalInfoLogService.getAdditionalInfo();
  }
  
  public FlowAttributesData getFlowAttributesData() {
    return this.mFlowAttributesData;
  }
  
  public ProxyInfo getProxyInfo() {
	  if (isProxyRequired()) {
		  return this.mProxyService.getProxy(ProxyType.SOCKS_PROXY, this.mSFTPBean.getCsftp_networkZone());
	  }
	  return this.mProxyService.getProxy(null, this.mSFTPBean.getCsftp_networkZone());
  }
  private boolean isProxyRequired() {
	    return !this.mSFTPBean.getCsftp_networkZone().equalsIgnoreCase("None");
	  }
	  
  protected boolean setProxySettings(String zone) {
    if (zone == null || getProxyService() == null)
      return false; 
    if (zone.equalsIgnoreCase("Default"))
      zone = "Private"; 
    try {
      if (getProxyService().isRemoteDnsResolutionEnabled(zone)) {
        this.session.setHost(this.mSFTPBean.getCsftpHost());
      } else {
        this.session.setHost(InetAddress.getByName(this.mSFTPBean.getCsftpHost()).getHostAddress());
      } 
    } catch (UnknownHostException e) {
      return false;
    } 
    ProxyInfo socksProxy = getProxyService().getProxy(ProxyType.SOCKS_PROXY, zone);
    ProxySOCKS5 prox = new ProxySOCKS5(socksProxy.getAddress().getHostName(), socksProxy.getAddress().getPort());
    if ((socksProxy.getUsername() != null && socksProxy.getUsername().length() > 0) || (socksProxy
      .getPassword() != null && socksProxy.getPassword().length() > 0)) {
      PasswordAuthentication authentication = Authenticator.requestPasswordAuthentication(socksProxy
          .getAddress().getAddress(), socksProxy.getAddress().getPort(), "SOCKS", null, null);
      if (authentication != null)
        prox.setUserPasswd(authentication.getUserName(), (authentication.getPassword() != null) ? new String(authentication.getPassword()) : null); 
    } 
    this.session.setProxy((Proxy)prox);
    return true;
  }
  
  public void setProxyService(ProxyService service) {
    this.mProxyService = service;
  }
  
  public ProxyService getProxyService() {
    return this.mProxyService;
  }
}

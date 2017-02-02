package ee.sk.smartid;

import ee.sk.smartid.exception.InvalidParametersException;
import ee.sk.smartid.rest.SessionStatusPoller;
import ee.sk.smartid.rest.SmartIdConnectorSpy;
import ee.sk.smartid.rest.dao.SessionSignature;
import ee.sk.smartid.rest.dao.SessionStatus;
import ee.sk.smartid.rest.dao.SignatureSessionResponse;
import org.junit.Before;
import org.junit.Test;

import static ee.sk.smartid.DummyData.createSessionEndResult;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SignatureRequestBuilderTest {

  private SmartIdConnectorSpy connector;
  private SessionStatusPoller sessionStatusPoller;
  private SignatureRequestBuilder builder;

  @Before
  public void setUp() throws Exception {
    connector = new SmartIdConnectorSpy();
    sessionStatusPoller = new SessionStatusPoller(connector);
    connector.signatureSessionResponseToRespond = createDummySignatureSessionResponse();
    connector.sessionStatusToRespond = createDummySessionStatusResponse();
    builder = new SignatureRequestBuilder(connector, sessionStatusPoller);
  }

  @Test
  public void signHashInBase64() throws Exception {
    SmartIdSignature signature = builder
        .withRelyingPartyUUID("relying-party-uuid")
        .withRelyingPartyName("relying-party-name")
        .withCertificateLevel("ADVANCED")
        .withHashType(HashType.SHA256)
        .withHashInBase64("jsflWgpkVcWOyICotnVn5lazcXdaIWvcvNOWTYPceYQ=")
        .withDocumentNumber("PNOEE-31111111111")
        .sign();
    assertCorrectSignatureRequestMade();
    assertCorrectSessionRequestMade();
    assertSignatureCorrect(signature);
  }

  @Test
  public void signWithSignableHash() throws Exception {
    SignableHash hashToSign = new SignableHash();
    hashToSign.setHashType(HashType.SHA256);
    hashToSign.setHashInBase64("jsflWgpkVcWOyICotnVn5lazcXdaIWvcvNOWTYPceYQ=");
    SmartIdSignature signature = builder
        .withRelyingPartyUUID("relying-party-uuid")
        .withRelyingPartyName("relying-party-name")
        .withCertificateLevel("ADVANCED")
        .withHash(hashToSign)
        .withDocumentNumber("PNOEE-31111111111")
        .sign();
    assertCorrectSignatureRequestMade();
    assertCorrectSessionRequestMade();
    assertSignatureCorrect(signature);
  }

  @Test
  public void signWithSignableData() throws Exception {
    SignableData dataToSign = new SignableData("Say 'hello' to my little friend!".getBytes());
    dataToSign.setHashType(HashType.SHA256);
    SmartIdSignature signature = builder
        .withRelyingPartyUUID("relying-party-uuid")
        .withRelyingPartyName("relying-party-name")
        .withCertificateLevel("ADVANCED")
        .withSignableData(dataToSign)
        .withDocumentNumber("PNOEE-31111111111")
        .sign();
    assertCorrectSignatureRequestMade();
    assertCorrectSessionRequestMade();
    assertSignatureCorrect(signature);

  }

  @Test(expected = InvalidParametersException.class)
  public void signWithoutDocumentNumber_shouldThrowException() throws Exception {
    builder
        .withRelyingPartyUUID("relying-party-uuid")
        .withRelyingPartyName("relying-party-name")
        .withCertificateLevel("ADVANCED")
        .withHashType(HashType.SHA256)
        .withHashInBase64("0nbgC2fVdLVQFZJdBbmG7oPoElpCYsQMtrY0c0wKYRg=")
        .sign();
  }

  @Test(expected = InvalidParametersException.class)
  public void signWithoutCertificateLevel_shouldThrowException() throws Exception {
    builder
        .withRelyingPartyUUID("relying-party-uuid")
        .withRelyingPartyName("relying-party-name")
        .withHashType(HashType.SHA256)
        .withHashInBase64("0nbgC2fVdLVQFZJdBbmG7oPoElpCYsQMtrY0c0wKYRg=")
        .withDocumentNumber("PNOEE-31111111111")
        .sign();
  }

  @Test(expected = InvalidParametersException.class)
  public void signWithoutHash_andWithoutData_shouldThrowException() throws Exception {
    builder
        .withRelyingPartyUUID("relying-party-uuid")
        .withRelyingPartyName("relying-party-name")
        .withCertificateLevel("ADVANCED")
        .withDocumentNumber("PNOEE-31111111111")
        .sign();
  }

  @Test(expected = InvalidParametersException.class)
  public void signWithoutHashType_shouldThrowException() throws Exception {
    builder
        .withRelyingPartyUUID("relying-party-uuid")
        .withRelyingPartyName("relying-party-name")
        .withCertificateLevel("ADVANCED")
        .withHashInBase64("0nbgC2fVdLVQFZJdBbmG7oPoElpCYsQMtrY0c0wKYRg=")
        .withDocumentNumber("PNOEE-31111111111")
        .sign();
  }

  @Test(expected = InvalidParametersException.class)
  public void signWithSignableHash_withoutHashType_shouldThrowException() throws Exception {
    SignableHash hashToSign = new SignableHash();
    hashToSign.setHashInBase64("0nbgC2fVdLVQFZJdBbmG7oPoElpCYsQMtrY0c0wKYRg=");
    builder
        .withRelyingPartyUUID("relying-party-uuid")
        .withRelyingPartyName("relying-party-name")
        .withCertificateLevel("ADVANCED")
        .withHash(hashToSign)
        .withDocumentNumber("PNOEE-31111111111")
        .sign();
  }

  @Test(expected = InvalidParametersException.class)
  public void signWithSignableHash_withoutHash_shouldThrowException() throws Exception {
    SignableHash hashToSign = new SignableHash();
    hashToSign.setHashType(HashType.SHA256);
    builder
        .withRelyingPartyUUID("relying-party-uuid")
        .withRelyingPartyName("relying-party-name")
        .withCertificateLevel("ADVANCED")
        .withHash(hashToSign)
        .withDocumentNumber("PNOEE-31111111111")
        .sign();
  }

  @Test(expected = InvalidParametersException.class)
  public void signWithoutRelyingPartyUuid_shouldThrowException() throws Exception {
    builder
        .withRelyingPartyName("relying-party-name")
        .withCertificateLevel("ADVANCED")
        .withHashType(HashType.SHA256)
        .withHashInBase64("0nbgC2fVdLVQFZJdBbmG7oPoElpCYsQMtrY0c0wKYRg=")
        .withDocumentNumber("PNOEE-31111111111")
        .sign();
  }

  @Test(expected = InvalidParametersException.class)
  public void signWithoutRelyingPartyName_shouldThrowException() throws Exception {
    builder
        .withRelyingPartyUUID("relying-party-uuid")
        .withCertificateLevel("ADVANCED")
        .withHashType(HashType.SHA256)
        .withHashInBase64("0nbgC2fVdLVQFZJdBbmG7oPoElpCYsQMtrY0c0wKYRg=")
        .withDocumentNumber("PNOEE-31111111111")
        .sign();
  }

  private void assertCorrectSignatureRequestMade() {
    assertEquals("PNOEE-31111111111", connector.documentNumberUsed);
    assertEquals("relying-party-uuid", connector.signatureSessionRequestUsed.getRelyingPartyUUID());
    assertEquals("relying-party-name", connector.signatureSessionRequestUsed.getRelyingPartyName());
    assertEquals("ADVANCED", connector.signatureSessionRequestUsed.getCertificateLevel());
    assertEquals("SHA256", connector.signatureSessionRequestUsed.getHashType());
    assertEquals("jsflWgpkVcWOyICotnVn5lazcXdaIWvcvNOWTYPceYQ=", connector.signatureSessionRequestUsed.getHash());
  }

  private void assertCorrectSessionRequestMade() {
    assertEquals("97f5058e-e308-4c83-ac14-7712b0eb9d86", connector.sessionIdUsed);
  }

  private void assertSignatureCorrect(SmartIdSignature signature) {
    assertNotNull(signature);
    assertEquals("luvjsi1+1iLN9yfDFEh/BE8h", signature.getValueInBase64());
    assertEquals("sha256WithRSAEncryption", signature.getAlgorithmName());
    assertEquals("PNOEE-31111111111", signature.getDocumentNumber());
  }

  private SignatureSessionResponse createDummySignatureSessionResponse() {
    SignatureSessionResponse response = new SignatureSessionResponse();
    response.setSessionId("97f5058e-e308-4c83-ac14-7712b0eb9d86");
    return response;
  }

  private SessionStatus createDummySessionStatusResponse() {
    SessionStatus status = new SessionStatus();
    status.setState("COMPLETE");
    status.setResult(createSessionEndResult());
    SessionSignature signature = new SessionSignature();
    signature.setValueInBase64("luvjsi1+1iLN9yfDFEh/BE8h");
    signature.setAlgorithm("sha256WithRSAEncryption");
    status.setSignature(signature);
    return status;
  }
}
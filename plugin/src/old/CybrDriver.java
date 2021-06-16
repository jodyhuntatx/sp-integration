/*
 * Test driver for Java REST API wrappers
 */

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.security.NoSuchAlgorithmException;
import java.security.KeyManagementException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.TimeUnit;
//import java.util.Base64;

import java.sql.*;

public class CybrDriver {

    public static Boolean DEBUG = Boolean.parseBoolean(System.getenv("DRIVER_DEBUG"));

    public static void main(String[] args) {

	String DB_URL = "jdbc:mysql://localhost/appgovdb";
	String DB_USER = "root";
	String DB_PASSWORD = "Cyberark1";

	String updateSql = "INSERT IGNORE INTO projects SET `name` = 'testproject', `admin` = 'jhunt'";
	String querySql = "SELECT * FROM projects";

	try (
	  Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
	  Statement stmt = conn.createStatement();
	) {
	  stmt.executeUpdate(updateSql); 

  	  ResultSet rs = stmt.executeQuery(querySql); 
          while (rs.next()) {
            System.out.print("Project Name: " + rs.getString("name"));
            System.out.println(", Admin: " + rs.getString("admin"));
	  }
        } catch (SQLException e) {
          e.printStackTrace();
        } 

  System.exit(0);

	// set to true to enable debug output
	PASJava.DEBUG = Boolean.parseBoolean(System.getenv("PASJAVA_DEBUG"));
	JavaREST.DEBUG = Boolean.parseBoolean(System.getenv("JAVAREST_DEBUG"));

	// turn off all cert validation - FOR DEMO ONLY
	disableSSL(); 
 
	// suppress "Illegal reflection" warnings
	JavaREST.disableAccessWarnings(); 

	// Initialize connection to PAS
	String pasServerIp= System.getenv("PAS_IIS_SERVER_IP");
	String pasAdminName = System.getenv("PAS_ADMIN_NAME");
	String pasAdminPwd = System.getenv("PAS_ADMIN_PASSWORD");
	PASJava.initConnection(pasServerIp);
	PASJava.logon(pasAdminName, pasAdminPwd);

	// Initialize connection to Conjur
	String conjurUrl = System.getenv("CONJUR_APPLIANCE_URL"); 
	String conjurAccount = System.getenv("CONJUR_ACCOUNT"); 
	ConjurJava.initConnection(conjurUrl, conjurAccount);

	String conjurAdminName = System.getenv("CONJUR_ADMIN_NAME"); 
	String conjurAdminPassword = System.getenv("CONJUR_ADMIN_PASSWORD"); 
	String conjurApiKey = ConjurJava.authnLogin(conjurAdminName, conjurAdminPassword);
	String conjurAuthnResponse = ConjurJava.authenticate(conjurAdminName, conjurApiKey);

        String vaultName  = System.getenv("PAS_VAULT_NAME");
	String safeName = System.getenv("PAS_SAFE_NAME");
        String cpmName  = System.getenv("PAS_CPM_NAME");
        String lobName  = System.getenv("PAS_LOB_NAME");
	String pasSafeAdminName = System.getenv("PAS_SAFE_ADMIN_NAME");
	String pasSafeAdminPwd = System.getenv("PAS_SAFE_ADMIN_PASSWORD");

	PASJava.addSafe(safeName, cpmName);
	PASJava.addSafeMember(safeName, lobName);
	PASJava.addSafeMember(safeName, pasSafeAdminName);

	String platformId = System.getenv("PAS_PLATFORM_ID");
	String accountName = System.getenv("PAS_ACCOUNT_NAME");
	String address = System.getenv("PAS_ACCOUNT_ADDRESS");
	String userName = System.getenv("PAS_ACCOUNT_USERNAME");
	String secretType = System.getenv("PAS_ACCOUNT_SECRET_TYPE");
	String secretValue = System.getenv("PAS_ACCOUNT_SECRET_VALUE");
	String keywords = System.getenv("PAS_ACCOUNT_SEARCH_KEYWORDS");
  	PASJava.addAccount(safeName,accountName,platformId,address,userName,secretType,secretValue);
  	PASJava.getAccounts(safeName);
//  	PASJava.getAccountDetails(accountId);
//  	PASJava.deleteAccount(safeId,accountId);
  	PASJava.getAccountGroups(safeName);
//  	PASJava.getAccountGroupMembers(safeName);

        String policyText = "---\n"
                        + "- !delete\n"
                        + "  record: !group " + vaultName
                        + "/" + lobName
                        + "/" + safeName
                        + "/delegation/consumers"
                        + "\n"
                        + "- !delete\n"
                        + "  record: !group " + vaultName
                        + "/" + lobName
                        + "/" + safeName
                        + "-admins"
                        + "\n"
                        + "- !delete\n"
                        + "  record: !policy " + vaultName
                        + "/" + lobName
                        + "/" + safeName;
        String delPolicyResponse = ConjurJava.loadPolicy("delete", "root", policyText);

	System.exit(0);

	try
	{
	  TimeUnit.SECONDS.sleep(60);
	}
	catch(InterruptedException ex)
	{
	  Thread.currentThread().interrupt();
	}

//	PASJava.logon(pasAdminName, pasAdminPwd);
//	PASJava.deleteSafe(safeName);

//  	PASJava.listSafes();

    } // main()

    // ==========================================
    // applyPolicy(accessRequest)
    //
    public static void applyPolicy(String _accessRequest) {
	String _vaultName = "vaultname";
	String _lobName = "lobname";
	String _safeName = "safename";

	if (CybrDriver.DEBUG) {
            System.out.println("Preloading sync policy:\n"
			 + "  Vault name: " + _vaultName + "\n"
			 + "  LOB name: " + _lobName + "\n"
			 + "  Safe name: " +_safeName);
	}

        ConjurJava.initConnection(
                                System.getenv("CONJUR_APPLIANCE_URL"),
                                System.getenv("CONJUR_ACCOUNT")
                                );
        String userApiKey = ConjurJava.authnLogin(
                                System.getenv("CONJUR_ADMIN_USERNAME"),
                                System.getenv("CONJUR_ADMIN_PASSWORD")
                                );
        ConjurJava.authenticate(
                                System.getenv("CONJUR_ADMIN_USERNAME"),
                                userApiKey
                                );

	// generate policy - REST method accepts text - no need to create a file
        String policyText = "---\n"
                            + "- !policy\n"
                            + "  id: " + _vaultName + "\n"
                            + "  body:\n"
                            + "  - !group " + _lobName + "-admins\n"
                            + "  - !policy\n"
                            + "    id: " + _lobName + "\n"
                            + "    owner: !group /" + _vaultName + "/" + _lobName + "-admins\n"
                            + "    body:\n"
                            + "    - !group " + _safeName + "-admins\n"
                            + "    - !policy\n"
                            + "      id: " + _safeName + "\n"
                            + "      body:\n"
                            + "      - !policy\n"
                            + "        id: delegation\n"
                            + "        owner: !group /" + _vaultName + "/" + _lobName + "/" + _safeName + "-admins\n"
                            + "        body:\n"
                            + "        - !group consumers\n";

        // load policy using default "append" method 
        ConjurJava.loadPolicy("append", "root", policyText);

    } // applyPolicy()

/*********************************************************
 *********************************************************
 **                    PRIVATE MEMBERS			**
 *********************************************************
 *********************************************************/

    // ==========================================
    // void disableSSL()
    //   from: https://nakov.com/blog/2009/07/16/disable-certificate-validation-in-java-ssl-connections/
    //
    private static void disableSSL() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
        };
 
        // Install the all-trusting trust manager
	try {
	        SSLContext sc = SSLContext.getInstance("SSL");
        	sc.init(null, trustAllCerts, new java.security.SecureRandom());
        	HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	} catch(NoSuchAlgorithmException e) {
		e.printStackTrace();
	} catch(KeyManagementException e) {
		e.printStackTrace();
	}

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
 
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

    } // disableSSL
 
    // ==========================================
    // void getRandomHexString()
    //
    private static String getRandomHexString(int numchars){
        Random r = new Random();
        StringBuffer sb = new StringBuffer();
        while(sb.length() < numchars){
            sb.append(String.format("%08x", r.nextInt()));
        }

        return sb.toString().substring(0, numchars);
    }

} // CybrDriver

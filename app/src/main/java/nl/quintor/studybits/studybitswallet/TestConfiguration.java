package nl.quintor.studybits.studybitswallet;

import android.net.Uri;

public class TestConfiguration {

    public static final String STUDENT_DID = "Xepuw1Y1k9DpvoSvZaoVJr";
    public static final String STUDENT_SECRET_NAME = "student_secret_name";
    public static final String STUDENT_SEED = "000000000000000000000000Student1";

    static String ENDPOINT_IP = BuildConfig.ENDPOINT_IP;
    static String ENDPOINT_GENT = "http://" + ENDPOINT_IP + ":8081";
    static String ENDPOINT_RUG = "http://" + ENDPOINT_IP + ":8080";

    static Uri CONNECTION_URI_RUG = Uri.parse("ssi://studybits?university=Rijksuniversiteit%20Groningen&did=SYqJSzcfsJMhSt7qjcQ8CC&endpoint=" + Uri.encode(TestConfiguration.ENDPOINT_RUG));
    static Uri CONNECTION_URI_GENT = Uri.parse("ssi://studybits?university=Universiteit%20Gent&did=Vumgc4B8hFq7n5VNAnfDAL&endpoint=" + Uri.encode(TestConfiguration.ENDPOINT_GENT));

}
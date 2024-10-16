package com.heroku.java;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Map;

@SpringBootApplication
@Controller
public class GettingStartedApplication {
    private final DataSource dataSource;

    @Autowired
    public GettingStartedApplication(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/{pathFragment}")
    public String echoUrl(@PathVariable String pathFragment, Model model) {
        model.addAttribute("path_fragment", pathFragment);
        return "result";
    }

    public String jacobi(float [][] a) {
        // Computes all eigenvalues and eigenvectors of a real symmetric matrix a, which is of size n by n, stored in a physical np by np array. On output, elements of a above the diagonal are
        // destroyed. d returns the eigenvalues of a in its first n elements. v is a matrix with the same
        // logical and physical dimensions as a, whose columns contain, on output, the normalized
        // eigenvectors of a. nrot returns the number of Jacobi rotations that were required.
        int n = a.length;
        float[][] v = new float[n][n];
        float[] b = new float[n];
        float[] d = new float[n];
        float[] z = new float[n];
        for (int ip = 0; ip < n; ip++) {
            v[ip][ip] = 1;
            b[ip] = a[ip][ip];
            d[ip] = b[ip];
            z[ip] = 0;
        }
        int nrot = 0;
        for (int i = 0; i < 50; i++) {
            float sm = 0;
            for (int ip = 0; ip < n - 1; ip++) {
                for (int iq = ip + 1; iq < n; iq++) {
                    sm += Math.abs(a[ip][iq]);
                }
            }
            if (sm == 0) return "Algo ended well.";
            double tresh;
            if (i < 4) {
                tresh = 0.2 * sm / Math.pow(n, 2);
            } else {
                tresh = 0;
            }
            for (int ip = 0; ip < n - 1; ip++) {
                for (int iq = ip + 1; iq < n; iq++) {
                    float g = 100 * Math.abs(a[ip][iq]);
//                  After four sweeps, skip the rotation if the off-diagonal element is small.
                    if (i > 3 && (Math.abs(d[ip]) + g == Math.abs(d[ip])) && (Math.abs(d[iq]) + g == Math.abs(d[iq]))) {
                        a[ip][iq] = 0;
                    } else if (Math.abs(a[ip][iq]) > tresh) {
                        float h = d[iq] - d[ip];
                        float t;
                        if (Math.abs(h) + g == Math.abs(h)) {
                            t = a[ip][iq] / h;
                        } else {
                            float theta = (float) 0.5 * h / a[ip][iq];
                            t = (float) 1 / (Math.abs(theta) + Math.sqrt(1 + Math.pow(theta, 2)));
                            if (theta < 0) t = -t;
                        }
                        float c = (float) 1 / Math.sqrt(1 + Math.pow(t, 2));
                        float s = t * c;
                        float tau = s / (1 + c);
                        h = t * a[ip][iq];
                        z[ip] = z[ip] - h;
                        z[iq] = z[iq] + h;
                        d[ip] = d[ip] - h;
                        d[iq] = d[iq] + h;
                        a[ip][iq] = 0;
                        for (int j = 0; j < ip - 1; j++) {
                            g = a[j][ip];
                            h = a[j][iq];
                            a[j][ip] = g - s * (h + g * tau);
                            a[j][iq] = h + s * (g - h * tau);
                        }
                        for (int j = ip + 1; j < iq - 1; j++) {
                            g = a[ip][j];
                            h = a[j][iq];
                            a[ip][j] = g - s * (h + g * tau);
                            a[j][iq] = h + s * (g - h * tau);
                        }
                        for (int j = iq + 1; j < n; j++) {
                            g = a[ip][j];
                            h = a[iq][j];
                            a[ip][j] = g - s * (h + g * tau);
                            a[iq][j] = h + s * (g - h * tau);
                        }
                        for (int j = 0; j < n; j++) {
                            g = v[j][ip];
                            h = v[j][iq];
                            v[j][ip] = g - s * (h + g * tau);
                            v[j][iq] = h + s * (g - h * tau);
                        }
                        nrot++;
                    }
                }
            }
            for (int ip= 0; ip < n; ip++) {
                b[ip] = b[ip] + z[ip];
                d[ip] = b[ip];
                z[ip] = 0;
            }
            return "Algo went thru too many iterations.";
        }
    }

    @GetMapping("/database")
    String database(Map<String, Object> model) {
        try (Connection connection = dataSource.getConnection()) {
            final var statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
            statement.executeUpdate("INSERT INTO ticks VALUES (now())");

            final var resultSet = statement.executeQuery("SELECT tick FROM ticks");
            final var output = new ArrayList<>();
            while (resultSet.next()) {
                output.add("Read from DB: " + resultSet.getTimestamp("tick"));
            }

            model.put("records", output);
            return "database";

        } catch (Throwable t) {
            model.put("message", t.getMessage());
            return "error";
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(GettingStartedApplication.class, args);
    }
}

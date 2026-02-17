package Nuvola.Projekatsiit2025.config;

import Nuvola.Projekatsiit2025.security.auth.RestAuthenticationEntryPoint;
import Nuvola.Projekatsiit2025.security.auth.TokenAuthenticationFilter;
import Nuvola.Projekatsiit2025.services.UserService;
import Nuvola.Projekatsiit2025.services.impl.UserServiceImpl;
import Nuvola.Projekatsiit2025.util.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity(debug = true)
@EnableMethodSecurity
public class WebSecurityConfig {
    // Handler za vracanje 401 kada klijent sa neodogovarajucim korisnickim imenom i lozinkom pokusa da pristupi resursu
    @Autowired
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    // Injektujemo implementaciju iz TokenUtils klase kako bismo mogli da koristimo njene metode za rad sa JWT u TokenAuthenticationFilteru
    @Autowired
    private TokenUtils tokenUtils;

    // Servis koji se koristi za citanje podataka o korisnicima aplikacije
//    @Bean
//    public UserDetailsService userDetailsService() {
//        return new UserServiceImpl();
//    }
    @Autowired
    private UserService userService;

    // Implementacija PasswordEncoder-a koriscenjem BCrypt hashing funkcije.
    // BCrypt po defalt-u radi 10 rundi hesiranja prosledjene vrednosti.
    //@Bean
    //public BCryptPasswordEncoder passwordEncoder() {
      //  return new BCryptPasswordEncoder();
    //}
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        // 1. koji servis da koristi da izvuce podatke o korisniku koji zeli da se autentifikuje
        // prilikom autentifikacije, AuthenticationManager ce sam pozivati loadUserByUsername() metodu ovog servisa
        authProvider.setUserDetailsService(userService);
        // 2. kroz koji enkoder da provuce lozinku koju je dobio od klijenta u zahtevu
        // da bi adekvatan hash koji dobije kao rezultat hash algoritma uporedio sa onim koji se nalazi u bazi (posto se u bazi ne cuva plain lozinka)
        authProvider.setPasswordEncoder(passwordEncoder);

        return authProvider;
    }

    // Registrujemo authentication manager koji ce da uradi autentifikaciju korisnika za nas
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // Definisemo prava pristupa za zahteve ka odredjenim URL-ovima/rutama
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults());
        http.csrf((csrf) -> csrf.disable());
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(restAuthenticationEntryPoint));
        http.authorizeHttpRequests(request -> {
            request.requestMatchers("/api/auth/login").permitAll()
                    .requestMatchers("/ws/**").permitAll()
                    .requestMatchers("/api/auth/register").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/auth/activate-email").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/auth/forgot-password").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/auth/reset-password").permitAll()
                    .requestMatchers("/api/foo").permitAll()
                    .requestMatchers("/api/drivers/active-vehicles").permitAll()
                    .requestMatchers("/api/rides/now/**").permitAll()
                    .requestMatchers("/api/drivers/active-vehicles").permitAll()
                    .requestMatchers("/api/drivers/*/rides").permitAll()
                    .requestMatchers("api/reviews").permitAll()
                    .requestMatchers("api/reviews/*").permitAll()
                    .requestMatchers("/api/support/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/drivers").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/drivers/*/picture").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/auth/activate").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/profile").permitAll()
                    .requestMatchers(HttpMethod.PUT, "/api/profile").permitAll()
                    .requestMatchers(HttpMethod.PUT, "/api/profile/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/profile/picture").permitAll()  // Upload
                    .requestMatchers(HttpMethod.GET, "/api/profile/picture/**").permitAll()  // Download - DODAJ OVO!
                    .requestMatchers(HttpMethod.GET, "/api/auth/reset-password/open").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/admin/users/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/admin/users/*/block").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/admin/users/*/unblock").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/rides").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/drivers/{username}/assigned-rides").permitAll()
                    .requestMatchers(HttpMethod.PUT, "/api/driver/profile/request-change").permitAll()
                    .requestMatchers(HttpMethod.PUT, "/api/driver/profile/request-change").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/admin/profile-change-requests").permitAll()
                    .requestMatchers(HttpMethod.PUT, "/api/admin/profile-change-requests/*/approve").permitAll()
                    .requestMatchers(HttpMethod.PUT, "/api/admin/profile-change-requests/*/reject").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/rides/active-ride").permitAll()
                    .requestMatchers(HttpMethod.PUT, "/api/rides/*/start").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/favorites").permitAll()
                    //Da nam lepsu poruku vrati
                    .requestMatchers("/error").permitAll()
                    //.requestMatchers(new AntPathRequestMatcher("/api/whoami")).hasRole("USER")
                    .anyRequest().authenticated();
        });
        http.addFilterBefore(new TokenAuthenticationFilter(tokenUtils, userService), UsernamePasswordAuthenticationFilter.class);
        http.authenticationProvider(authenticationProvider());
        return http.build();
    }
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // Autentifikacija ce biti ignorisana ispod navedenih putanja (kako bismo ubrzali pristup resursima)
        // Zahtevi koji se mecuju za web.ignoring().antMatchers() nemaju pristup SecurityContext-u
        // Dozvoljena POST metoda na ruti /auth/login, za svaki drugi tip HTTP metode greska je 401 Unauthorized
        return (web) -> web.ignoring()//.requestMatchers(HttpMethod.POST, "/auth/login")


                // Ovim smo dozvolili pristup statickim resursima aplikacije
                .requestMatchers(HttpMethod.GET, "/", "/webjars/*", "/*.html", "favicon.ico",
                        "/*/*.html", "/*/*.css", "/*/*.js");

    }
    //Podesavanja CORS-a
    //https://docs.spring.io/spring-security/reference/servlet/integrations/cors.html
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("POST", "PUT", "GET", "OPTIONS", "DELETE", "PATCH")); // or simply "*"
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

package com.id.px3.auth;

import com.id.px3.rest.security.JwtService;
import com.id.px3.utils.DurationParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@ComponentScan(basePackages = "com.id.px3")
@Slf4j
public class Px3AuthApplication implements CommandLineRunner {

	private final JwtService jwtService;

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(Px3AuthApplication.class);
		app.run(args);
	}

	public Px3AuthApplication(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	public void run(String... args) {
		Options options = buildCliOptions();

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd;

		//  remove from args any spring.* option
		args = Arrays.stream(args)
				.filter(arg -> !arg.startsWith("--spring."))
				.toArray(String[]::new);

		try {
			cmd = parser.parse(options, args, true);
			if (cmd.hasOption("g")) {
                //  JWT generation mode
                String username = null;
                if (cmd.hasOption("u")) {
                    //  get username
                    username = cmd.getOptionValue("u");
                } else {
                    log.error("Missing required options: Username is required");
                    exitWithHelp(formatter, options);
                }


                String[] rolesArray = new String[0];
                if (cmd.hasOption("r")) {
                    rolesArray = cmd.getOptionValue("r").split(",");
                }

                Duration expiration = null;
                if (cmd.hasOption("e")) {
                    expiration = DurationParser.parse(cmd.getOptionValue("e"));
                } else {
                    log.error("Missing required options: roles and/or expire");
                    exitWithHelp(formatter, options);
                }

                //  generate JWT
                String jwt = jwtService.generateToken(username, new HashSet<>(Arrays.asList(rolesArray)), expiration);
                System.out.println("Generated JWT: " + jwt);

                //  exit after JWT generation with success
                System.exit(0);
            }
		} catch (Exception e) {
			exitWithHelp(formatter, options);
		}
	}

	private static void exitWithHelp(HelpFormatter formatter, Options options) {
		formatter.printHelp("napa-fi-agent", options);
		//  exit with failure
		System.exit(1);
	}

	private static Options buildCliOptions() {
		Options options = new Options();

		Option generateJwt = new Option("g", "generate-jwt", false, "Generate a JWT token");
		options.addOption(generateJwt);

		Option username = new Option("u", "username", true, "User name");
		username.setRequired(false);
		options.addOption(username);

		Option roles = new Option("r", "roles", true, "Roles array (comma-separated)");
		roles.setRequired(false);
		options.addOption(roles);

		Option expiration = new Option("e", "expire", true, "Expiration (1d = 1 day, 365d = 1 year, ...)");
		expiration.setRequired(false);
		options.addOption(expiration);
		return options;
	}
}
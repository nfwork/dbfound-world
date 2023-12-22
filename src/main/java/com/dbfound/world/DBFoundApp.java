package com.dbfound.world;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DBFoundApp
{
    public static void main( String[] args )
    {
    	SpringApplication.run(DBFoundApp.class, args);
        System.out.println( "Hello dbfound world!" );
    }
}
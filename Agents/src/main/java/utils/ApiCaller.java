package utils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

import com.google.gson.Gson;

import entities.GameStatus;

public class ApiCaller {
    //public static void main(String[] args) {
    public String callGetApi(String apiUrl){
        // Default API endpoint
        if(apiUrl == null)
            apiUrl = "http://localhost:8080/api/get-room?Id=8c3a6a717fbc4a1abfc28b520ddd69b7";
        
        // Create HttpClient with timeout
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        // Build GET request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            // Send request and get response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Check HTTP status code
            if (response.statusCode() == 200) {
                //System.out.println("Success! Response:");
                //System.out.println(response.body());
                return response.body();
            } else {
                System.out.println("Request failed. HTTP Status: " + response.statusCode());
                System.out.println("Response body: " + response.body());
                return null;
            }

        } catch (HttpTimeoutException e) {
            System.err.println("Request timed out: " + e.getMessage());
            return null;
        } catch (IOException | InterruptedException e) {
            System.err.println("Error during API call: " + e.getMessage());
            Thread.currentThread().interrupt(); // Restore interrupt status if interrupted
            return null;
        }
    }
    
    public String callPutApi(String apiUrl, Object body) {
    	if(apiUrl == null)
            apiUrl = "http://localhost:8080/api/change-current-team";
        // Create HttpClient with timeout
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        // Build GET request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(new Gson().toJson(body)))
                .build();

        try {
            // Send request and get response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Check HTTP status code
            if (response.statusCode() == 200) {
                System.out.println("Success! Response:" + response.body());
                //System.out.println();
                return response.body();
            } else {
                System.out.println("Request failed. HTTP Status: " + response.statusCode());
                System.out.println("Response body: " + response.body());
                return null;
            }

        } catch (HttpTimeoutException e) {
            System.err.println("Request timed out: " + e.getMessage());
            return null;
        } catch (IOException | InterruptedException e) {
            System.err.println("Error during API call: " + e.getMessage());
            Thread.currentThread().interrupt(); // Restore interrupt status if interrupted
            return null;
        }
    }

    public GameStatus getGameStatus(String gameId){
        GameStatus res = new  Gson().fromJson(callGetApi("http://localhost:8080/api/get-room?Id=" + gameId), GameStatus.class);
        return res;
    }
}
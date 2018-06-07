package com.mst.dtnserver;

import com.mst.karsac.Roger.RogerIp;
import com.mst.karsac.connections.ImageMessage;
import com.mst.karsac.messages.Messages;
import com.mst.karsac.ratings.MessageRatings;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ServerJava {

    public static final int PORT = 8080;
    public static final String PATH = "C:\\Users\\ks2ht\\Documents\\DTN\\";

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                try {
                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                    RogerIp obj = (RogerIp) ois.readObject();
                    System.out.println(obj.my_macaddress);
                    System.out.println(obj.imageMessages.size());
                    File directory = new File(PATH);
                    createFolder(directory);
                    String user_folder = PATH + obj.my_macaddress + "\\";
                    File new_user = new File(user_folder);
                    createFolder(new_user);
                    Calendar now = Calendar.getInstance();
                    int month = now.get(Calendar.MONTH) + 1;
                    int date = now.get(Calendar.DATE);
                    System.out.println("Month:" + month + " Date:" + date);
                    String date_path = user_folder + month + "_" + date + "\\";
                    File date_folder = new File(date_path);
                    createFolder(date_folder);
                    ArrayList<ImageMessage> imageMessages = obj.imageMessages;
                    for (ImageMessage imageMessage : imageMessages) {
                        Messages img_msg = imageMessage.messages;
                        String file_path = date_path + img_msg.uuid + ".txt";
                        PrintWriter writer = new PrintWriter(file_path, "UTF-8");
                        writer.println("Source mac:" + img_msg.sourceMac);
                        writer.println("File name:" + img_msg.fileName);
                        writer.println("Format:" + img_msg.format);
                        writer.println("Tags:" + img_msg.tagsForCurrentImg);
                        writer.println("Timestamp:" + img_msg.timestamp);
                        writer.println("Latitude:" + img_msg.lat);
                        writer.println("Longitude:" + img_msg.lon);
                        writer.println("Size:" + img_msg.size);
                        writer.println("Incentive Paid:" + img_msg.incentive_paid);
                        writer.println("Incentive received:" + img_msg.incentive_received);
                        writer.println("Incentive promised:" + img_msg.incentive_promised);
                        writer.println("intermediaries separated by \"|\" :" + img_msg.destAddr);
                        writer.println("-------Ratings for this message----------");

                        List<MessageRatings> messageRatings = imageMessage.messageRatings;
                        for(MessageRatings ratings : messageRatings){
                            writer.println("Unique_id: " + ratings.getIntermediary());
                            String type = ratings.getInter_type();
                            writer.println("Type: " + type);
                            writer.println("Tag rate: " + ratings.getTag_rate());
                            writer.println("Confidence rate: " + ratings.getConfidence_rate());
                            if(type.contains("Source")){
                                writer.println("Quality rate: " + ratings.getQuality_rate());
                            }
                            writer.println("-----------------------------------------");
                        }
                        if(messageRatings.size() == 0) {
                            writer.println("No ratings added for this message!");
                            writer.println("-----------------------------------------");
                        }
                        writer.close();
                        String img_path = date_path + img_msg.uuid + ".jpg";
                        File img_file = new File(img_path);
                        decodeBase64String(imageMessage.img_path, img_file);
                    }

                } finally {
                    socket.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void createFolder(File files) {
        if (!files.exists()) {
            boolean check = files.mkdirs();
            System.out.println(check);
        }
    }

    public static void decodeBase64String(String img_string, File image) {
        try (FileOutputStream imageOutFile = new FileOutputStream(image)) {
            // Converting a Base64 String into Image byte array
            byte[] imageByteArray = org.apache.commons.codec.binary.Base64.decodeBase64(img_string);
            imageOutFile.write(imageByteArray);
        } catch (FileNotFoundException e) {
            System.out.println("Image not found" + e);
        } catch (IOException ioe) {
            System.out.println("Exception while reading the Image " + ioe);
        }
    }
}

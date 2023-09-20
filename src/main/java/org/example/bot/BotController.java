package org.example.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendVideo;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.pengrad.telegrambot.model.request.ParseMode.HTML;
import static org.example.bot.JedisActions.*;


public class BotController {

    public static JedisPool jedisPool;
    public static final String USER_DB_MAP_KEY = "userDBMap";
    public static ArrayList<Long> allUsers = new ArrayList<>();

    public static void main(String[] args) throws URISyntaxException {
        String TOKEN = "";
        String AdminID = "710511911";
        try {
            String configFilePath = "src/config.properties";
            FileInputStream propsInput = new FileInputStream(configFilePath);
            Properties prop = new Properties();
            prop.load(propsInput);
            TOKEN = prop.getProperty("TOKEN");

        } catch (IOException e) {
            e.printStackTrace();
        }

        String redisUriString = System.getenv("REDIS_URL");
        jedisPool = new JedisPool(new URI(redisUriString));

        TelegramBot bot = new TelegramBot(TOKEN);

        bot.setUpdatesListener(updates -> {
            try (Jedis jedis = jedisPool.getResource()) {
                updates.forEach(update -> {
                    String playerName = "Trader";
                    long playerId;
                    String messageText = "";
                    String messageCallbackText = "";
                    String uid;
                    int messageId;
//                    Path resourcePath = Paths.get("src/main/resources");
//                    File videoDepositFile = resourcePath.resolve("depositTutorial.mp4").toFile();
//                    File videoRegistrationFile = resourcePath.resolve("videoRegistrationGuide.mp4").toFile();
//                    File videoExampleFile = resourcePath.resolve("videoExample.mp4").toFile();


                    if (update.callbackQuery() == null && (update.message() == null || update.message().text() == null)) {
                        return;
                    }

                    if (update.callbackQuery() == null) {
                        playerName = update.message().from().firstName();
                        playerId = update.message().from().id();
                        messageText = update.message().text();
                        messageId = update.message().messageId();
                    } else if (update.message() == null) {
                        playerName = update.callbackQuery().from().firstName();
                        playerId = update.callbackQuery().from().id();
                        messageCallbackText = update.callbackQuery().data();
                        messageId = update.callbackQuery().message().messageId();
                    } else {
                        messageId = 0;
                        playerId = 0L;
                    }

                    if (playerId != Long.parseLong(AdminID)) {
                        try {
                            String userKey = USER_DB_MAP_KEY + ":" + playerId;
                            User checkedUser = convertJsonToUser(jedis.get(userKey));
                            Date date = new Date();
                            checkedUser.setLastTimeTexted(date);
                            String updatedUser = convertUserToJson(checkedUser);
                            jedis.set(userKey, updatedUser);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

//
//                    String userKeyAdmin = USER_DB_MAP_KEY + ":" + AdminID;
//                    String userKeyIm = USER_DB_MAP_KEY + ":" + "430823029";
//                    Date adminDate = new Date();
//                    User adminUser = new User("Admin", "64", false, false, adminDate, adminDate, 1, false, false, false);
//                    jedis.set(userKeyAdmin, convertUserToJson(adminUser));
                    //        User I'm = new User("NoAdmin", "430823029", true, true, adminDate, 1, true);
                    //       jedis.set(userKeyIm, convertUserToJson(Im));


                    try {
                        String userKey = USER_DB_MAP_KEY + ":" + AdminID;
                        User checkedAdmin = convertJsonToUser(jedis.get(userKey));
                        Date currentDate = new Date();
                        Date checkAdminDate = DateUtil.addDays(checkedAdmin.getLastTimeTexted(), 2);
                        System.out.println("Im not there");
                        if (checkAdminDate.getTime() < currentDate.getTime()) {
                            System.out.println("Im there");
                            checkedAdmin.setLastTimeTexted(currentDate);
                            jedis.set(userKey, convertUserToJson(checkedAdmin));
                            System.out.println("Admin done");
                            Set<String> userKeys = jedis.keys("userDBMap:*");
                            System.out.println("Keys done");
                            System.out.println(userKeys.size());
                            for (String keyForUser : userKeys) {
                                User currentUser = convertJsonToUser(jedis.get(keyForUser));
                                if (currentUser.getLastTimeTexted() != null && currentUser.getTimesTextWasSent() != 0) {
                                    Date checkUserDate = DateUtil.addDays(currentUser.getLastTimeTexted(), 1);
                                    if (checkUserDate.getTime() < currentDate.getTime()) {
                                        String userTgID = keyForUser.substring(10);
                                        if (currentUser.isDeposited() && currentUser.getTimesTextWasSent() == 1) {
                                            bot.execute(new SendMessage(userTgID, "\uD83D\uDD14 I received an update, now my signals have become even more accurate! This is a great opportunity to earn money. Try trading with me for the next 8 hours. ").parseMode(HTML));
                                            increaseTimesWasSent(keyForUser);
                                        } else if (currentUser.isDeposited() && currentUser.getTimesTextWasSent() == 2) {
                                            bot.execute(new SendMessage(userTgID, "\uD83D\uDD14 The market is in a fantastic state at the moment. It's the ideal time to trade and make easy money! Only 4 hours left until the market is awesome.").parseMode(HTML));
                                            increaseTimesWasSent(keyForUser);
                                        } else if (currentUser.isRegistered() && currentUser.getTimesTextWasSent() == 1) {
                                            bot.execute(new SendMessage(userTgID, "\uD83D\uDD14 The final step to receiving signals is left! Everything can be done quickly and conveniently for you! If you encounter any issues while depositing, please review the video above. Also use promo code 50START to receive bonus to your deposit.").parseMode(HTML));
                                            increaseTimesWasSent(keyForUser);
                                        } else if (currentUser.isRegistered() && currentUser.getTimesTextWasSent() == 2) {
                                            bot.execute(new SendMessage(userTgID, "\uD83D\uDD14 It seems you still don't want to start earning. After depositing, you will gain access to my accurate signals. I'm not human, but my analysis indicates that you're making a mistake by not working with me.").parseMode(HTML));
                                            increaseTimesWasSent(keyForUser);
                                        } else if (!currentUser.isRegistered() && currentUser.getTimesTextWasSent() == 1) {
                                            bot.execute(new SendMessage(userTgID, "\uD83D\uDD14 I want to remind you that for registration, you need to create a new account using this link: https://bit.ly/ChatGPTtrading. It won't take more than 2 minutes. You can also review the video above, it should help you. I'm ready to give you my signals.").parseMode(HTML));
                                            increaseTimesWasSent(keyForUser);
                                        } else if (!currentUser.isRegistered() && currentUser.getTimesTextWasSent() == 2) {
                                            bot.execute(new SendMessage(userTgID, "\uD83D\uDD14 I want to remind you that signing up doesn't require much time! Just make a new account using this link: https://bit.ly/ChatGPTtrading. (This is the final reminder, if you don't manage to create an account within the next 3 days, you won't get access to my signals)").parseMode(HTML));
                                            increaseTimesWasSent(keyForUser);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (String.valueOf(playerId).equals(AdminID)) {
                        if (messageText.startsWith("A") || messageText.startsWith("a") || messageText.startsWith("Ф") || messageText.startsWith("ф")) {
                            try {
                                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                                InlineKeyboardButton button7 = new InlineKeyboardButton("Deposit done!");
                                button7.callbackData("IDeposit");
                                inlineKeyboardMarkup.addRow(button7);
                                System.out.println(messageText.length());
                                String tgID = messageText.substring(1);
                                System.out.println(tgID);
                                registrationApprove(Long.parseLong(tgID));
                                bot.execute(new SendMessage(tgID, "✅ Great, your account is confirmed! The last step is to make any deposit at least 50$ by any convenient way. After that press the button 'Deposit done'.\n" +
                                        "\n" +
                                        "I would like to note that the recommended starting deposit of $50 - $350. Also use promo code 50START to get an extra 50% of your deposit. For example, with a deposit of 100$ you will get 50$ additional. It means that you will get 150$ in total.\n" +
                                        "\n" +
                                        "At the bottom there is a video instruction on how to top up the account.").replyMarkup(inlineKeyboardMarkup));
                        //        bot.execute(new SendVideo(tgID, videoDepositFile));
                                bot.execute(new SendMessage(tgID, "☝️ Here is a video guide on how to make a deposit.").parseMode(HTML));
                                bot.execute(new SendMessage(AdminID, "Registration for " + tgID + " was approved"));
                                setTo1TimesWasSent(tgID);
                            } catch (Exception e) {
                                bot.execute(new SendMessage(AdminID, "❌ There was an issue. Please try again. "));
                                e.printStackTrace();
                            }
                        } else if (messageText.startsWith("reply:")) {
                            int indexOfAnd = messageText.indexOf("&");
                            String tgID = messageText.substring(6, indexOfAnd);
                            String reply = messageText.substring(indexOfAnd + 1);
                            System.out.println(indexOfAnd + "\n" + tgID + "\n" + reply);
                            bot.execute(new SendMessage(tgID, reply));
                            bot.execute(new SendMessage(AdminID, "Reply was sent"));
                        } else if (messageText.startsWith("deleteUser:")) {
                            try {
                                String TGId = USER_DB_MAP_KEY + ":" + (messageText.substring(11));
                                jedis.del(TGId);
                                bot.execute(new SendMessage(AdminID, "User with ID " + TGId + " was fully deleted"));
                            } catch (Exception e) {
                                bot.execute(new SendMessage(AdminID, "❌ There was an issue. Please try again. "));
                                e.printStackTrace();
                            }
                        } else if (messageText.startsWith("banSupport:")) {
                            try {
                                String TGId = USER_DB_MAP_KEY + ":" + (messageText.substring(11));
                                User userBanned = convertJsonToUser(jedis.get(TGId));
                                userBanned.setCanWriteToSupport(true);
                                String updatedBannedUser = convertUserToJson(userBanned);
                                jedis.set(TGId, updatedBannedUser);
                                bot.execute(new SendMessage(AdminID, "User with ID " + TGId + " was banned to write to support"));
                            } catch (Exception e) {
                                bot.execute(new SendMessage(AdminID, "❌ There was an issue. Please try again. "));
                                e.printStackTrace();
                            }
                        } else if (messageText.startsWith("banDeposit30:")) {
                            try {
                                String TGId = USER_DB_MAP_KEY + ":" + (messageText.substring(13));
                                User userBanned = convertJsonToUser(jedis.get(TGId));
                                Date currentDate = new Date();
                                userBanned.setLastTimePressedDeposit(DateUtil.addMinutes(currentDate, 30));
                                String updatedBannedUser = convertUserToJson(userBanned);
                                jedis.set(TGId, updatedBannedUser);
                                bot.execute(new SendMessage(AdminID, "User with ID " + TGId + " was banned to press button 'Deposit done' for 30 minutes. "));
                            } catch (Exception e) {
                                bot.execute(new SendMessage(AdminID, "❌ There was an issue. Please try again. "));
                                e.printStackTrace();
                            }
                        } else if (messageText.startsWith("deleteDeposit:")) {
                            try {
                                String TGId = (messageText.substring(14));
                                depositDisapprove(Long.parseLong(TGId));
                                System.out.println(TGId);
                                bot.execute(new SendMessage(AdminID, "User with ID " + TGId + " got deleted"));
                            } catch (Exception e) {
                                bot.execute(new SendMessage(AdminID, "❌ There was an issue. Please try again.  "));
                                e.printStackTrace();
                            }
                        } else if (messageText.startsWith("deleteRegistration:")) {
                            try {
                                String TGId = (messageText.substring(19));
                                registrationDisapprove(Long.parseLong(TGId));
                                System.out.println(TGId);
                                bot.execute(new SendMessage(AdminID, "User with ID " + TGId + " got register disapprove"));
                            } catch (Exception e) {
                                bot.execute(new SendMessage(AdminID, "❌ There was an issue. Please try again. "));
                                e.printStackTrace();
                            }
                        } else if (messageText.startsWith("getUserName:")) {
                            try {
                                String TGId = USER_DB_MAP_KEY + ":" + (messageText.substring(12));
                                User newUser = convertJsonToUser(jedis.get(TGId));
                                bot.execute(new SendMessage(AdminID, "Name of user is: " + newUser.getName() + " his TG id: " + TGId));
                            } catch (Exception e) {
                                bot.execute(new SendMessage(AdminID, "❌ There was an issue. Please try again. "));
                                e.printStackTrace();
                            }
                        } else if (messageText.equals("/clearDB")) {
                            try {
                                jedis.flushAll();
                                bot.execute(new SendMessage(AdminID, "DB was cleaned"));
                            } catch (Exception e) {
                                bot.execute(new SendMessage(AdminID, "❌ There was an issue. Please try again. "));
                                e.printStackTrace();
                            }
                        } else if (messageText.equals("/getAllUsers")) {
                            int size = 141 + allUsers.size();
                            bot.execute(new SendMessage(AdminID, "There is " + size + " users right now."));
                        } else if (messageText.startsWith("setCheckForUID:")) {
                            try {
                        //        long newCheck = Integer.parseInt(messageText.substring(15));
                        //        User adminUser = convertJsonToUser(jedis.get(AdminID));
                         //       adminUser.setUID(String.valueOf(newCheck));
                         //       String updatedAdminUser = convertUserToJson(adminUser);
                          //      jedis.set(AdminID, updatedAdminUser);
                          //      bot.execute(new SendMessage(AdminID, "First numbers is: " + newCheck + "."));
                            } catch (Exception e) {
                                bot.execute(new SendMessage(AdminID, "❌ There was an issue. Please try again. "));
                                e.printStackTrace();
                            }
                        } else if (messageText.startsWith("createNewPost:")) {
                            try {
                                String postText = messageText.substring(14);
                                Set<String> userKeys = jedis.keys("userDBMap:*");
                                System.out.println("Amount of users: " + userKeys.size());
                                for (String keyForUser : userKeys) {
                                    String userTgID = keyForUser.substring(10);
                                    bot.execute(new SendMessage(userTgID, postText));
                                }
                                bot.execute(new SendMessage(AdminID, "The message " + postText + " has been sent."));
                            } catch (Exception e) {
                                bot.execute(new SendMessage(AdminID, "❌ There was an issue. Please try again. "));
                                e.printStackTrace();
                            }
                        } else if (messageText.startsWith("D") || messageText.startsWith("d") || messageText.startsWith("В") || messageText.startsWith("в")) {
                            String tgID = messageText.substring(1);
                            InlineKeyboardButton button12 = new InlineKeyboardButton("Register here");
                            InlineKeyboardButton button13 = new InlineKeyboardButton("I'm ready!");
                            button12.url("https://bit.ly/ChatGPTtrading");
                            button13.callbackData("ImRegistered");
                            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                            inlineKeyboardMarkup.addRow(button12, button13);
                            bot.execute(new SendMessage(tgID, "❌ Something went wrong. Make sure you registered with the 'Register here' button and sent a new UID. There is an example of how to do it step by step in the video below. After that press the 'I'm ready!'\n" +
                                    "\n" +
                                    "If you still have problems, then write to support with the command /support. ").replyMarkup(inlineKeyboardMarkup));
                            bot.execute(new SendMessage(AdminID, "Registration for " + tgID + " was disapproved"));
                        } else if (messageText.startsWith("Y") || messageText.startsWith("y") || messageText.startsWith("Н") || messageText.startsWith("н")) {
                            try {
                                String tgID = messageText.substring(1);
                                depositApprove(Long.parseLong(tgID));
                                Keyboard replyKeyboardMarkup = (Keyboard) new ReplyKeyboardMarkup(
                                        new String[]{"Get Signal"});
                                bot.execute(new SendMessage(AdminID, "Deposit for " + tgID + " was approved"));
                                bot.execute(new SendMessage(tgID, "✅ Great! Everything is ready! You can start getting signals. For this click on 'Get Signal' or write it manually. \n" +
                                        "\n" +
                                        "Below is a video guide on how to use signals from me. \n" +
                                        "\n" +
                                        "If you have any questions use the /support command.").replyMarkup(replyKeyboardMarkup));
                                setTo1TimesWasSent(tgID);
                            } catch (Exception e) {
                                bot.execute(new SendMessage(AdminID, "❌ There was an issue. Please try again. "));
                                e.printStackTrace();
                            }
                        } else if (messageText.startsWith("N") || messageText.startsWith("n") || messageText.startsWith("Т") || messageText.startsWith("т")) {
                            String tgID = messageText.substring(1);
                            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                            InlineKeyboardButton button7 = new InlineKeyboardButton("Deposit done");
                            button7.callbackData("IDeposit");
                            inlineKeyboardMarkup.addRow(button7);
                            bot.execute(new SendMessage(tgID, "❌ Something went wrong. Make sure you deposited at least 50$ to the new account you created through the link and then click 'Deposit done' ").replyMarkup(inlineKeyboardMarkup));
                            bot.execute(new SendMessage(AdminID, "Deposit for " + tgID + " was disapproved"));
                        }
                    } else if (messageText.startsWith("needReply:")) {
                        String userQuestion = messageText.substring(10);
                        if (!userCanWriteToSupport(playerId)) {
                            bot.execute(new SendMessage(playerId, "✅ Our admin will reply you shortly!" + userQuestion).parseMode(HTML));
                            bot.execute(new SendMessage(AdminID, "✅ ID:<code>" + playerId + "</code> has a question" + userQuestion + " To answer it write a message: <code>reply:111111111&</code> *your text*").parseMode(HTML));
                        } else {
                            bot.execute(new SendMessage(playerId, "❌ There was an issue. The support is currently unavailable. Please try again later. ").parseMode(HTML));
                        }
                    } else if (messageText.equals("/help") || messageCallbackText.equals("Help")) {
                        bot.execute(new SendMessage(playerId, "\uD83D\uDC4B Welcome to Customer Support! If you have a question, please use the command #question: Copy the command <code>#question:</code> , " +
                                "paste <code>#question:</code> in the chat, write your question, send the message, and wait for our admin to respond shortly. \uD83D\uDCE9").parseMode(HTML));
                    } else if (messageText.equals("/start")) {
                        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                        InlineKeyboardButton button32 = new InlineKeyboardButton("Let's start");
                        button32.callbackData("RegisterMe");
                        inlineKeyboardMarkup.addRow(button32);
                        if (!allUsers.contains(playerId)) {
                            allUsers.add(playerId);
                        }
                        bot.execute(new SendMessage(playerId, "\uD83D\uDC4B Hey, " + playerName + "\n" +
                                "\n" +
                                "\uD83D\uDCC8 I am the HighAccuracyTrade Bot, and I am created to provide highly accurate trading signals. I use market analysis to calculate the probabilities of where currency pairs might go. All you need to do is simply copy my signals and start making money!\uD83D\uDCC8 \n" +
                                "\n" +
                                "To begin receiving signals, just start by clicking on 'Next Step!'. \uD83D\uDCCA\n").replyMarkup(inlineKeyboardMarkup).parseMode(HTML));
                        bot.execute(new SendMessage(playerId, "If you ever run into any issues or have suggestions, you can reach out to our bot support using the /help command. ❗\uFE0F").parseMode(HTML));
                    } else if (userDeposited(playerId) || userDeposited(playerId)) {
                        if (messageText.equals("/newSignal") || messageCallbackText.equals("getSignal") || messageText.equals("/newsignal")) {
                            List<String> listOfPairs = Arrays.asList(
                                    "AUD/CAD OTC", "AUD/CHF OTC", "AUD/NZD OTC", "CAD/CHF OTC", "EUR/CHF OTC",
                                    "EUR/JPY OTC", "EUR/USD OTC", "GBP/JPY OTC", "NZD/JPY OTC", "NZD/USD OTC",
                                    "USD/CAD OTC", "USD/CNH OTC", "CHF/NOK OTC", "EUR/GBP OTC", "EUR/TRY OTC",
                                    "CHF/JPY OTC", "EUR/NZD OTC", "AUD/JPY OTC", "AUD/USD OTC", "EUR/HUF OTC",
                                    "USD/CHF OTC"
//                                    "CHF/JPY","GBP/AUD","AUD/USD","EUR/GBP","GBP/CAD","GBP/JPY","EUR/AUD",
//                                    "GBP/CHF","CAD/CHF","CAD/JPY","EUR/CHF","USD/JPY","AUD/JPY","EUR/CAD","AUD/CHF","AUD/CAD","USD/CAD","USD/CNH","EUR/JPY"
                            );
                            Runnable signalGeneratorTask = () -> {
                                bot.execute(new SendMessage(playerId, "\uD83D\uDFE2").parseMode(HTML));
                                try {
                                    Thread.sleep(1500);
                                } catch (InterruptedException e) {
                                    bot.execute(new SendMessage(playerId, "❌ An error occurred. Please try again. "));
                                    e.printStackTrace();
                                }
                                Random random = new Random();
                                int randomNumber = random.nextInt(listOfPairs.size());
                                int randomUp = random.nextInt(2);
                                String direction = "";
                                if (randomUp == 0) {
                                    direction = "\uD83D\uDFE2⬆\uFE0F Signal: <b>UP</b> ";
                                } else {
                                    direction = "\uD83D\uDD34⬇\uFE0F Signal: <b>DOWN</b> ";
                                }
                                //int randomAccuracy = random.nextInt(20) + 80;
                                int randomAccuracy = 99;
                                int randomAddTime = random.nextInt(10000) + 8000;
                                //   int randomTime = random.nextInt(3) + 1;
                                int randomTime =  1;
                                String pickedPair = listOfPairs.get(randomNumber);
                                EditMessageText editMessageText = new EditMessageText(playerId, messageId + 1, "\uD83D\uDFE2\uD83D\uDFE2").parseMode(HTML);
                                bot.execute(editMessageText);
                                try {
                                    Thread.sleep(1500);
                                } catch (InterruptedException e) {
                                    bot.execute(new SendMessage(playerId, "❌ An error occurred. Please try again. "));
                                    e.printStackTrace();
                                }
                                EditMessageText editMessageTex = new EditMessageText(playerId, messageId + 1, "\uD83D\uDFE2\uD83D\uDFE2\uD83D\uDFE2").parseMode(HTML);
                                bot.execute(editMessageTex);
                                try {
                                    Thread.sleep(1500);
                                } catch (InterruptedException e) {
                                    bot.execute(new SendMessage(playerId, "❌ An error occurred. Please try again. "));
                                    e.printStackTrace();
                                }
                                EditMessageText editMessageText4 = new EditMessageText(playerId, messageId + 1, "\uD83D\uDCC8The <b>" + pickedPair + "</b> asset is currently being analyzed.").parseMode(HTML);
                                bot.execute(editMessageText4);
                                try {
                                    Thread.sleep(3000);
                                } catch (InterruptedException e) {
                                    bot.execute(new SendMessage(playerId, "❌ An error occurred. Please try again. "));
                                    e.printStackTrace();
                                }
                                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                                InlineKeyboardButton button22 = new InlineKeyboardButton("Get new signal");
                                button22.callbackData("getSignal");
                                inlineKeyboardMarkup.addRow(button22);
                                EditMessageText editMessage = new EditMessageText(playerId, messageId + 1, "\uD83D\uDE80 Trading Alert: \n\uD83D\uDCC8 Asset: <b>" + pickedPair + "</b>\n" + direction + "\n⏳ Duration: <b> " + randomTime + " Minutes </b>\n\uD83C\uDFAF Accuracy: <b> " + randomAccuracy + "%</b>\n\n\uD83D\uDEA6 Trading Tips:\n" +
                                        "\uD83D\uDC41\u200D\uD83D\uDDE8 Await the 'Start!' signal,\n" +
                                        "\uD83D\uDCAC Make your prediction,\n" +
                                        "\uD83D\uDCBC Secure your position, and\n" +
                                        "\uD83D\uDCB0 Trade smartly!").parseMode(HTML);
                                bot.execute(editMessage);
                                try {
                                    Thread.sleep(randomAddTime);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                Keyboard replyKeyboardMarkup = (Keyboard) new ReplyKeyboardMarkup(
                                        new String[]{"/newSignal"});
                                bot.execute(new SendMessage(playerId, "<b>Start!</b>").parseMode(HTML));
                            };
                            new Thread(signalGeneratorTask).start();
                        } else if (messageText.equals("/changeMode") || messageText.equals("/changemode")){
                            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                            InlineKeyboardButton button22 = new InlineKeyboardButton("OTC");
                            button22.callbackData("OTC");
                            InlineKeyboardButton button23 = new InlineKeyboardButton("None OTC");
                            button23.callbackData("noneOTC");
                            InlineKeyboardButton button24 = new InlineKeyboardButton("Both");
                            button24.callbackData("both");
                            inlineKeyboardMarkup.addRow(button22, button23, button24);
                            bot.execute(new SendMessage(playerId, "<b>Please choose your mode!</b>").parseMode(HTML).replyMarkup(inlineKeyboardMarkup));
                        } else if (messageCallbackText.equals("OTC")){
                            bot.execute(new SendMessage(playerId, "<b>\uD83D\uDFE2 You successfully picked 'OTC' mode! Now you will get only OTC signals. To change it use /changeMode command.</b>").parseMode(HTML));
                        } else if (messageCallbackText.equals("noneOTC")){
                            bot.execute(new SendMessage(playerId, "<b>\uD83D\uDFE2 You successfully picked 'None OTC' mode! Now you will get only none OTC signals. To change it use /changeMode command.</b>").parseMode(HTML));
                        } else if (messageCallbackText.equals("both")){
                            bot.execute(new SendMessage(playerId, "<b>\uD83D\uDFE2 You successfully picked 'Both' mode! Now you will get all available signals. To change it use /changeMode command.</b>").parseMode(HTML));
                        }
                    } else if (userRegistered(playerId)) {
                        if (messageCallbackText.equals("IDeposit")) {
                            try {
                                Date currentDate = new Date();
                                String userKey = USER_DB_MAP_KEY + ":" + playerId;
                                User checkedUser = convertJsonToUser(jedis.get(userKey));
                                Date userDate = checkedUser.getLastTimePressedDeposit();
                                if (userDate == null) {
                                    checkedUser.setLastTimePressedDeposit(currentDate);
                                    String updatedUser = convertUserToJson(checkedUser);
                                    jedis.set(userKey, updatedUser);
                                    String sendAdminUID = checkedUser.getUID();
                                    bot.execute(new SendMessage(Long.valueOf(AdminID), "User with Telegram ID<code>" + playerId + "</code> and UID <code>" + sendAdminUID + "</code> \uD83D\uDFE1 deposited. Write 'Y11111111' (telegram id) to approve and 'N1111111' to disapprove").parseMode(HTML));
                                    bot.execute(new SendMessage(playerId, "\uD83D\uDCE9 Great your deposit will be checking soon."));
                                } else {
                                    if (userDate.getTime() <= currentDate.getTime()) {
                                        String sendAdminUID = checkedUser.getUID();
                                        bot.execute(new SendMessage(Long.valueOf(AdminID), "User with Telegram ID<code>" + playerId + "</code> and UID <code>" + sendAdminUID + "</code> \uD83D\uDFE1 deposited. Write 'Y11111111' (telegram id) to approve and 'N1111111' to disapprove").parseMode(HTML));
                                        bot.execute(new SendMessage(playerId, "\uD83D\uDCE9 Great your deposit will be checking soon."));
                                    } else {
                                        bot.execute(new SendMessage(playerId, "\uD83D\uDCE9 Please wait 30 minutes before next time pressing button."));
                                    }
                                }


                            } catch (Exception e) {
                                bot.execute(new SendMessage(playerId, "❌ There was an issue. Please try again. "));
                                e.printStackTrace();
                            }
                        } else if (userDeposited(playerId)) {
                            bot.execute(new SendMessage(playerId, "❌ There was an issue. Please try again. "));
                        } else if (messageText.startsWith("/") || messageText.equals("Get Signal")) {
                            bot.execute(new SendMessage(playerId, "Before trying any signals you need to deposit"));
                        }
                    } else {
                        if (messageText.equals("/register") || messageCallbackText.equals("RegisterMe")) {
                            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                            InlineKeyboardButton button2 = new InlineKeyboardButton("Register here");
                            InlineKeyboardButton button3 = new InlineKeyboardButton("I'm ready!");
                            button2.url("https://bit.ly/ChatGPTtrading");
                            button3.callbackData("ImRegistered");
                            inlineKeyboardMarkup.addRow(button2, button3);
                            bot.execute(new SendMessage(playerId, "✅ Great job! To get started, you need to create a new account on the Pocket Option platform through the button below. \n" +
                                    "\n" +
                                    " \uD83D\uDD17 bit.ly/ChatGPTtrading \n" +
                                    "\n" +
                                    "\uD83D\uDCD7 After registering, click on the 'I'm ready!\n" +
                                    "\n" +
                                    "⚠️ Be sure to register using the button 'Register' below or link from the message. Otherwise, the bot will not be able to confirm that you have joined the team. \n" +
                                    "\n" +
                                    "‼️ It's important to note that if you already have an existing Pocket Option account, it's possible to delete and create a new one, and after you can go through the personality verification process again in your new account. This process of deleting and creating a new account is authorized and permitted by Pocket Option administrators.").replyMarkup(inlineKeyboardMarkup).parseMode(HTML).disableWebPagePreview(true));
         //
                        } else if (messageCallbackText.equals("ImRegistered")) {
                            bot.execute(new SendMessage(playerId, "✅ Good job! Now send me your Pocket Option ID in format 'ID12345678'.").parseMode(HTML));
                        } else if (messageText.startsWith("ID") || messageText.startsWith("id") || messageText.startsWith("Id") || messageText.startsWith("iD") && messageText.length() == 10 || messageText.length() == 11) {
                            try {
                                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                                InlineKeyboardButton button5 = new InlineKeyboardButton("Yes");
                                InlineKeyboardButton button6 = new InlineKeyboardButton("No");
                                button5.callbackData("YesIM");
                                button6.callbackData("ImRegistered");
                                inlineKeyboardMarkup.addRow(button5, button6);
                                String text = messageText.replaceAll("\\s", "");
                                uid = text.substring(2, 10);
                                Date date = new Date();
                                Date depositDate = DateUtil.addDays(date, -1);
                                User newUser = new User(playerName, uid, false, false, date, depositDate, 1, false, false, false);
                                bot.execute(new SendMessage(playerId, "\uD83D\uDCCC Your ID is " + uid + " is it correct?").replyMarkup(inlineKeyboardMarkup).parseMode(HTML));
                                String userKey = USER_DB_MAP_KEY + ":" + playerId;
                                jedis.set(userKey, convertUserToJson(newUser));
                            } catch (Exception e) {
                                bot.execute(new SendMessage(playerId, "❌ There was an issue. Please try again.  "));
                                e.printStackTrace();
                            }
                        } else if (messageText.startsWith("user") || messageText.startsWith("USER") && messageText.length() == 12 || messageText.length() == 13) {
                            try {
                                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                                InlineKeyboardButton button5 = new InlineKeyboardButton("Yes");
                                InlineKeyboardButton button6 = new InlineKeyboardButton("No");
                                button5.callbackData("YesIM");
                                button6.callbackData("ImRegistered");
                                inlineKeyboardMarkup.addRow(button5, button6);
                                String text = messageText.replaceAll("\\s", "");
                                uid = text.substring(4, 12);
                                Date date = new Date();
                                Date depositDate = DateUtil.addDays(date, -1);
                                User newUser = new User(playerName, uid, false, false, date, depositDate, 1, false, false, false);
                                bot.execute(new SendMessage(playerId, "\uD83D\uDCCC Your ID is " + uid + " is it correct?").replyMarkup(inlineKeyboardMarkup).parseMode(HTML));
                                String userKey = USER_DB_MAP_KEY + ":" + playerId;
                                jedis.set(userKey, convertUserToJson(newUser));
                            } catch (Exception e) {
                                bot.execute(new SendMessage(playerId, "❌ There was an issue. Please try again.  "));
                                e.printStackTrace();
                            }
                        } else if (messageCallbackText.equals("YesIM")) {
                            String userKey = USER_DB_MAP_KEY + ":" + playerId;
                            try {
                                User user = convertJsonToUser(jedis.get(userKey));
                                String sendAdminUID = user.getUID();
                                User adminUser2 = convertJsonToUser(jedis.get(AdminID));
                                if (Integer.parseInt(sendAdminUID.substring(0, 2)) >= Integer.parseInt("60")) {
                                    bot.execute(new SendMessage(Long.valueOf(AdminID), "User with Telegram ID<code>" + playerId + "</code> and UID <code>" + sendAdminUID + "</code> \uD83D\uDFE2 want to register. Write 'A11111111' (telegram id) to approve and 'D1111111' to disapprove").parseMode(HTML));
                                    bot.execute(new SendMessage(playerId, "⏳ Great, your UID will be verified soon"));
                                } else {
                                    InlineKeyboardButton button12 = new InlineKeyboardButton("Register here");
                                    InlineKeyboardButton button13 = new InlineKeyboardButton("I'm ready!");
                                    button12.url("https://bit.ly/ChatGPTtrading");
                                    button13.callbackData("ImRegistered");
                                    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                                    inlineKeyboardMarkup.addRow(button12, button13);
                                    bot.execute(new SendMessage(playerId, "❌ Something went wrong. You sent me old UID. Make sure you registered with the 'Register here' button and sent a new UID. There is an example of how to do it step by step in the video below. After that press the 'I'm ready!'\n" +
                                            "\n" +
                                            "If you still have problems, then write to support with the command /support. ").replyMarkup(inlineKeyboardMarkup));
                                }
                            } catch (Exception e) {
                                bot.execute(new SendMessage(playerId, "❌ There was an issue. Please try again.  "));
                                e.printStackTrace();
                            }
                        } else if (messageText.startsWith("/") || messageText.equals("Get Signal")) {
                            bot.execute(new SendMessage(playerId, "Before trying any signals you need to register"));
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }

            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            jedisPool.close();
        }));
    }


}

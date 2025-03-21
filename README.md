![image alt](https://github.com/GA-Projects9/TradeSphere/blob/bc629ee47b090f47d723029fac4ae9d13bcbc52b/Screenshot%202025-03-21%20165645.png)

TradeSphere is a versatile online marketplace, designed for users to seamlessly buy and sell a diverse range of products or property, whether new or second-hand. The app provides a platform for users to create, sell, buy or manage their ads, allowing direct communication through in-app messaging, calls, or popular messaging services like SMS and WhatsApp. With a user-friendly interface and features such as adding ads to favorites, TradeSphere allows people to connect, negotiate, and do their trade within a dynamic and user-driven marketplace. 

TradeSphere does not involve payment integration as selling apps usually restrict online payments due to scam since the person selling is not a huge enterprise/any other business for most of the time, and are usually just individuals reselling products they’ve brought/have or running their own business for. 

The app allows browsing before signing up/loging in, but other features such as chatting, account information, selling and my ads options are not available unless the individual has an account. If the individual clicks on any of those features prior to signing up/in, then they’re redirected to the login page, which includes options for recovering password and registering as well.

I’ve used Firebase as my database, Canva for logo design, Flaticon for stickers relating to UI, and Figma for wireframing. I used Java for backend and frontend, and also used github repositories for other features necessary of my app. The chat backend was also done using Firebase with the help of Firebase Cloud Messaging. 

USER INTERFACE, DESIGN and DEVELOPMENT

![image alt](https://github.com/GA-Projects9/TradeSphere/blob/bc629ee47b090f47d723029fac4ae9d13bcbc52b/Screenshot%202025-03-21%20170104.png)

I’ve made use of a simple design that involves different shades of purple for my app theme, as well as engaging stickers that pops the design overall. The app has a bottom menu navigation that was inspired by similar selling apps. The app opens and displays 3 options. Email Login has options to Register and Recover Password. 

![image alt](https://github.com/GA-Projects9/TradeSphere/blob/bc629ee47b090f47d723029fac4ae9d13bcbc52b/Screenshot%202025-03-21%20170135.png)
The user can also choose Phone Login, which takes you to Phone Login page that has a field for entering the users phone number, and upon clicking on ‘Send OTP’, takes the user to next page for the pin code verification through the otp the user received. If the user didn’t receive the OTP, there is a resend OTP option as well. Password Recovery option asks the user to enter their email for receiving further instructions. After login, the user is prompted to the main page, where different ads and categories are displayed in RecyclerView, as well as bottom navigation menu to move between each of the navigation options. There is an auto image slider that displays three different images for visual representation of the app.

![image alt](https://github.com/GA-Projects9/TradeSphere/blob/bc629ee47b090f47d723029fac4ae9d13bcbc52b/Screenshot%202025-03-21%20170204.png)

The My Ads navigation contains two tabs: Ads Tab for ads that the user created, and the Favorites tab for ads that the user selected as favorite. The Chats navigation consist of a chat list - the individuals the user has messaged/communicated with. Upon opening the chat, the user is able to see the previous messages that the user has sent to the seller/buyer, which includes images as well.

![image alt](https://github.com/GA-Projects9/TradeSphere/blob/bc629ee47b090f47d723029fac4ae9d13bcbc52b/Screenshot%202025-03-21%20170232.png)

The user can sell an ad by clicking on the plus icon, which prompts the user to the create ad page. It displays various options for writing down brand, category, condition, description etc. of the product that the user wants to sell. The Ad Details page is simple and includes all the information of the ad required with the images in a slider; it also includes the options for Chat, Call and SMS for the Seller. It also displays the Sellers description below. Seller Profile page is simple, and just shows the number of ads that the seller has published as well as displays the ads in a list.

![image alt](https://github.com/GA-Projects9/TradeSphere/blob/bc629ee47b090f47d723029fac4ae9d13bcbc52b/Screenshot%202025-03-21%20170253.png)

The account navigation of the user is simple consiting of various information for the user, as well as the preferences option for the user to choose. Edit profile page allows the user to edit name, email, phone number (depending on the users login method, it differs), and can be updated. The ResetPasswordActivity simply displays instructions to display password, initially asking the user to confirm current password and validation, before setting the new password. The DeleteAccountActivity has a warning label done through cardview, and a confirm delete option. 

REFLECTION & DISCUSSION

Overall, I’m pretty satisfied with what I’ve created, but since given the chance to assess, I would like to fix the editing of ad. As of now, there is a duplication of images when the user edits the ad by adding more images, although it is not visible after updating the ad. I also add an issue with adding the jitpack repository to the build file  for the auto image slider implementation since the format wasn’t right, as it was changed for the latest android versions. I would also like to fix how the FCM notifications work, as the notification is shown for every single message, even if you have the chat open. Also, I had initially thought of using a custom progress bar but since I started with progress dialog which is a deprecated function due to convenience, I couldn’t end up doing it either, which I’m unsatisfied with.

CONCLUSION

If there is something that I would like to add or improve, then it would be adding location to the app. I had tried implementing it, but came across a wall when I realized that integrating Places API is a paid service. In the future, I would like to implement it using any other apis that’s available for location. Also, allowing the users to choose from a set of images for profile that is stored in firebase. Adding dark mode/light mode, progress bar, payment integration (which is unavailable for TradeSphere as selling apps usually restrict online payments due to scam since the person selling is not a huge enterprise/any other business for most of the time, and are usually just individuals reselling products they’ve brought/have). I would also like to separate the chats between different tabs for Sellers and Buyers. 

Thank you for reading!

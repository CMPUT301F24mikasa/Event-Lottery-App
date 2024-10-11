**List of Potential Classes and Methods with inputs and outputs**

*1. Notifications:*

> CONSTRUCTOR:
<space><space>*<space>    String text
<space><space>*<space>    
<space><space>*<space>    

> METHODS:
<space><space>*<space>    enable (better to implement in User class instead?)
<space><space>*<space>    disable (better to implement in User class instead?)


*2. Settings:*

> CONSTRUCTOR:
<space><space>*<space>
<space><space>*<space>
<space><space>*<space>

> METHODS:
<space><space>*<space>
<space><space>*<space>
<space><space>*<space>

*3. User Shuffler:*

> CONSTRUCTOR:
<space><space>*<space>
<space><space>*<space>
<space><space>*<space>

> METHODS:
<space><space>*<space>
<space><space>*<space>
<space><space>*<space>

*4. Events:*

> CONSTRUCTOR:
<space><space>*<space>
<space><space>*<space>
<space><space>*<space>

> METHODS:
<space><space>*<space>
<space><space>*<space>
<space><space>*<space>

*5. Admin:*

> CONSTRUCTOR:
<space><space>*<space>
<space><space>*<space>
<space><space>*<space>

> METHODS:
<space><space>*<space>
<space><space>*<space>
<space><space>*<space>

*6. User:*

> CONSTRUCTOR:
<space><space>*<space> String name
<space><space>*<space> String email
<space><space>*<space> String phone number (create a separate constructor including phone number, because it's optional)
<space><space>*<space> Profile picture (custom/auto)
<space><space>*<space> Notification list (initially empty)
<space><space>*<space> Notifications Toggle (T/F)

> METHODS:
<space><space>*<space>    enable/disable() : for a particular notification (inputs: notification object, true/false)
<space><space>*<space>    profile_pic_generator() : automatically creates a profile picture and adds it to user object somehow

<space><space>*<space>    notification_lst_updater (inputs: notification object) : [if notification toggle True == update; else == return void] 
<space><space>*<space>    notication_opt_out() : Turns the Notification Toggle to False


*7. (to be continued)*

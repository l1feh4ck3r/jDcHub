package ru.sincore;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sincore.TigerImpl.CIDGenerator;
import ru.sincore.util.AdcUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

/**
 * Configuration Manager
 *
 * @author hatred
 *         <p/>
 *         Date: 28.09.11
 *         Time: 15:30
 */
public class ConfigurationManager extends PropertiesConfiguration
{
    private static final Logger log = LoggerFactory.getLogger(ConfigurationManager.class);

    private            String hubConfigDir = "./etc";

    // TODO: try more fine method to point config file
    public static final String HUB_CONFIG = "hub.properties";

    /*
     *  Config keys constants
     */
    // General
    public static final String HUB_LISTEN = "core.listen.network";
    public static final String HUB_PORT   = "core.listen.port";

    // Big static text file storage, DB or FILE, if selected FILE, needed to specify file path
    // Available DB and FILE; default FILE;
    public static final String BIG_FILE_STORAGE      = "core.hub.big_file_storage";
    public static final String FILE_STORAGE_LOCATION = "core.hub.file_storage_location";
    public static final String ABOUT_FILE            = "core.hub.about_file";
    public static final String LICENSE_FILE          = "core.hub.license_file";
    public static final String MOTD_FILE             = "core.hub.motd_file";
    public static final String RULES_FILE            = "core.hub.rules_file";

    // Default en_US if client not present self locale, all messages must be show default locale
    public static final String HUB_DEFAULT_LOCALE = "core.hub.default_locale";

    public static final String MAX_USERS                  = "core.hub.max_users";
    public static final String DISCONNECT_ONLINE_WITH_SAME_CID = "core.hub.disconnect_online_with_same_cid";
    public static final String DISCONNECT_BY_TIMEOUT      = "core.hub.disconnect_by_timeout";
    public static final String MAX_KEEP_ALIVE_TIMEOUT     = "core.hub.max_keep_alive_timeout";
    public static final String USER_INITIAL_CAPACITY      = "core.hub.user_initial_capacity";
    public static final String USER_LOAD_FACTOR           = "core.hub.user_load_factor";
    public static final String USER_CONNECTION_BUFFER_INITIAL_SIZE = "core.hub.user_connection_buffer_initial_size";
    public static final String MAX_HUBS_USERS             = "core.hub.max_hubs_users";
    public static final String MAX_HUBS_REGISTERED        = "core.hub.max_hubs_registered";
    public static final String MAX_SHARE_SIZE             = "core.hub.max_share_size";
    public static final String MIN_SHARE_SIZE             = "core.hub.min_share_size";
    public static final String BAN_BY_SHARE_MIN_SHARE     = "core.hub.ban_by_share.min_share_size";
    public static final String MAX_NICK_SIZE              = "core.hub.max_nick_size";
    public static final String MIN_NICK_SIZE              = "core.hub.min_nick_size";
    public static final String MIN_SLOT_COUNT             = "core.hub.min_slot_count";
    public static final String MAX_SLOT_COUNT             = "core.hub.max_slot_count";
    public static final String MAX_EMAIL_CHAR_COUNT       = "core.hub.max_email_char_count";
    public static final String MAX_DESCRIPTION_CHAR_COUNT = "core.hub.max_description_char_count";
	public static final String MIN_PASSWORD_LEN           = "core.hub.min_password_len";

    public static final String THREADS_CORE_POOL_SIZE     = "core.hub.threads.core_pool_size";
    public static final String THREADS_MAXIMUM_POOL_SIZE  = "core.hub.threads.maximum_pool_size";
    public static final String THREADS_KEEP_ALIVE_TIME    = "core.hub.threads.keep_alive_time";

    public static final String MESSAGE_BUFFER_SIZE        = "core.hub.message_buffer.size";
    public static final String MESSAGE_BUFFER_FLUSH_PERIOD= "core.hub.message_buffer.flush_period";

             // Maximum description char count
    public static final String MAX_OP_IN_HUB              = "core.hub.max_op_in_chat";
            // Maximum OP in chat (needed ?)
    // Maximum available char count in chat messages received.
    public static final String MAX_CHAT_MESSAGE_SIZE      = "core.hub.max_chat_message_size";
    public static final String HUB_NAME                   = "core.hub.name";
    public static final String HUB_DESCRIPTION            = "core.hub.description";
    public static final String HUB_OWNER                  = "core.hub.owner";
    public static final String HUB_VERSION                = "core.hub.version";
    public static final String HUB_GREETING               = "core.hub.greeting";
    // Available nickname char
    public static final String NICK_CHAR                  = "core.hub.nick_chars";
    public static final String REDIRECT_URL               = "core.hub.redirect_url";
    // The main redirect URL to send faulty users ( or default redirects )
    public static final String HUB_SID                    = "core.hub.sid";

    /** Command engine ***************************************************************************/
    public static final String OP_COMMAND_PREFIX          = "core.hub.command.engine.op_prefix";
    public static final String USER_COMMAND_PREFIX        = "core.hub.command.engine.user_prefix";
    // If true, return actionName result in private chat
    public static final String COMMAND_PM_RETURN          = "core.hub.command.pm_return";
    public static final String COMMAND_SAVE_LOG           = "core.hub.save_log";
    public static final String COMMAND_SAVE_LOG_TO_DB     = "core.hub.command.save_log_to_db";


    /** Search settings **************************************************************************/
    public static final String MIN_CHARS_SEARCH_REQUEST  = "core.hub.min_char_search_request";
    public static final String MAX_CHARS_SEARCH_REQUEST  = "core.hub.max_char_search_request";
    //Save log in DB
    public static final String SAVE_SEARCH_LOG           = "core.hub.save_search_log";
    //Interval between automagic searches for each user, seconds, (?)
    public static final String AUTOMATIC_SEARCH_INTERVAL = "core.hub.automatic_search_interval";
    //Maximum nr of search steps allowed until reset needed       (?)
    public static final String SEARCH_STEPS              = "core.hub.search_step";
    //Logarithmic base for user searches interval, millis         (?)
    public static final String SEARCH_BASE_INTERVAL      = "core.hub.search_base_interval";
    //Interval until search_steps is being reset, seconds		  (?)
    public static final String SEARCH_SPAM_RESET         = "core.hub.search_spam_reset";

    public static final String FEATURED_BROADCAST        = "core.hub.broadcast.featured";

    // Timeouts
    public static final String HUB_LOGIN_TIMEOUT      = "core.hub.login.timeout";
    // Time in millisecond to kick duration
    public static final String KICK_DURATION          = "core.hub.kick_duration";

    // Chat settings
    public static final String CHAT_REFRESH           = "core.hub.chat_refresh";
    public static final String CHAT_MESSAGE_INTERVAL  = "core.hub.chat.message_interval";
    public static final String CHAT_SAME_MESSAGE_SPAM_INTERVAL = "core.hub.chat.same_message_spam_interval";

    // History messages line count
    public static final String LAST_MESSAGES_COUNT    = "last.messages.count";
    public static final String OP_CHAT_NAME           = "core.hub.op_chat_name";
    public static final String OP_CHAT_DESCRIPTION    = "core.hub.op_chat_description";
    public static final String OP_CHAT_WEIGHT         = "core.hub.op_chat_weight";
    public static final String OP_CHAT_SID            = "core.hub.op_chat_sid";
    public static final String BOT_CHAT_NAME          = "core.hub.bot_chat_name";
    public static final String BOT_CHAT_DESCRIPTION   = "core.hub.bot_chat_description";
    public static final String BOT_CHAT_WEIGHT        = "core.hub.bot_chat_weight";
    public static final String BOT_CHAT_SID           = "core.hub.bot_chat_sid";
    public static final String VIP_CHAT_NAME          = "core.hub.vip_chat_name";
    public static final String VIP_CHAT_DESCRIPTION   = "core.hub.vip_chat_description";
    public static final String VIP_CHAT_WEIGHT        = "core.hub.vip_chat_weight";
    public static final String VIP_CHAT_SID           = "core.hub.vip_chat_sid";
    public static final String REG_CHAT_NAME          = "core.hub.reg_chat_name";
    public static final String REG_CHAT_DESCRIPTION   = "core.hub.reg_chat_description";
    public static final String REG_CHAT_WEIGHT        = "core.hub.reg_chat_weight";
    public static final String REG_CHAT_SID           = "core.hub.reg_chat_sid";

    // Mark chat or actionName for registration users only
    public static final String MARK_REGISTRATION_ONLY = "core.hub.mark_registration_only";

    // AdcUtils Extensions
    // space separated adc extension list, send in first message to connected client.
    public static final String ADC_EXTENSION_LIST = "core.hub.adc_extension_list";
    // Enable or disable adcs extensions support;
    public static final String ENABLE_ADCS        = "core.hub.extension.adcs.adcs_enable";
    // Mmm.. maybe it certificate authorisation ? - no forgot add in properties
    public static final String CERT_LOGIN         = "core.hub.extension.certificate_login_enable";

    // Localisations
    public static final String HUB_MESSAGES_FILE_DIR        = "core.client.messages.dir";
    // Client weights
    public static final String CLIENT_WEIGHT_UNREGISTRED    = "core.client.client_type_weights.unregistered";
    public static final String CLIENT_WEIGHT_REGISTRED      = "core.client.client_type_weights.registered";
    public static final String CLIENT_WEIGHT_OPERATOR       = "core.client.client_type_weights.operator";
    public static final String CLIENT_WEIGHT_SUPER_USER     = "core.client.client_type_weights.super_user";
    public static final String CLIENT_WEIGHT_HUB_OWNER      = "core.client.client_type_weights.hub_owner";

    public static final String USE_WORD_FILTER              = "core.word_filter.use";
    public static final String USE_WORD_FILTER_IN_PM        = "core.word_filter.use_in_pm";

    public static final String SCRIPTS_LOCATION                 = "core.hub.scripts.directory";
    public static final String NUMBER_OF_SCRIPTS_INTERPRETERS   = "core.hub.scripts.interpreter.instances";

    // RXTX extension options
    public static final String RXTX_TIME_PERIOD             = "core.hub.rxtx_time_period";


    // Internal options
    public static final String OP_CHAT_CID  = "internal.op_chat_cid";
    public static final String VIP_CHAT_CID = "internal.vip_chat_cid";
    public static final String REG_CHAT_CID = "internal.reg_chat_cid";
    public static final String BOT_CHAT_CID = "internal.bot_chat_cid";
    public static final String SECURITY_CID = "internal.security_cid";

    /*
     * Private fields
     */
    private static ConfigurationManager instance = null;


    /*
    * Ctors
    */
    private ConfigurationManager(String configDir)
    {
        this.hubConfigDir = configDir;
        if (!loadConfigs())
        {
            log.error("Fatal error! Can\'t load configs. Hub doesn't started.");
            System.exit(0);
        }
    }


    private ConfigurationManager()
    {
        if (!loadConfigs())
        {
            log.error("Fatal error! Can\'t load configs. Hub doesn't started.");
            System.exit(0);
        }
    }


    public static synchronized ConfigurationManager getInstance()
    {
        if (instance == null)
        {
            instance = new ConfigurationManager();
        }
        return instance;
    }


    public static synchronized ConfigurationManager getInstance(String configDir)
    {
        if (instance == null)
        {
            instance = new ConfigurationManager(configDir);
        }
        return instance;
    }


    public String getAdcString(String key)
    {
        return AdcUtils.toAdcString(super.getString(key));
    }

    public synchronized boolean loadConfigs()
    {
        File hubPropertiesFile;
        InputStream fileInput;
        InputStreamReader reader;

        try
        {
            hubPropertiesFile = new File(hubConfigDir + "/" + HUB_CONFIG);
            fileInput = new FileInputStream(hubPropertiesFile);
            reader = new InputStreamReader(fileInput);

            this.load(reader);
        }
        catch (Exception e)
        {
            log.error("Error >>>", e);
            return false;
        }

        return true;
    }


    public String getHubConfigDir()
    {
        return hubConfigDir;
    }


    public void setHubConfigDir(String configDir)
    {
        this.hubConfigDir = configDir;
    }


    public synchronized void load(java.io.Reader in)
            throws org.apache.commons.configuration.ConfigurationException
    {
        log.info("Reloaded load(Reader in) is called");

        super.load(in);

        /*
         * Apply some cheks and restrictions
         */

        // OP_CHAT_CID
        this.setProperty(OP_CHAT_CID, CIDGenerator.generate());
        this.setProperty(VIP_CHAT_CID, CIDGenerator.generate());
        this.setProperty(REG_CHAT_CID, CIDGenerator.generate());
        this.setProperty(BOT_CHAT_CID, CIDGenerator.generate());

        // SECURITY_CID
        this.setProperty(SECURITY_CID, CIDGenerator.generate());

        // HUB_SID
        Pattern sidPattern = Pattern.compile("^[A-Z]{4}$");
        String hubSid = this.getString(HUB_SID);
        if (!sidPattern.matcher(hubSid).matches())
        {
            this.setProperty(HUB_SID, "ABCD");
        }

        // BOT_CHAT_SID
        String botChatSid = this.getString(BOT_CHAT_SID);
        if (!sidPattern.matcher(botChatSid).matches())
        {
            this.setProperty(BOT_CHAT_SID, "DCBA");
        }

        // OP_CHAT_SID
        String opChatSid = this.getString(OP_CHAT_SID);
        if (!sidPattern.matcher(opChatSid).matches())
        {
            this.setProperty(OP_CHAT_SID, "ABCE");
        }

        // VIP_CHAT_SID
        String vipChatSid = this.getString(VIP_CHAT_SID);
        if (!sidPattern.matcher(vipChatSid).matches())
        {
            this.setProperty(VIP_CHAT_SID, "ABCF");
        }

        // REG_CHAT_SID
        String regChatSid = this.getString(REG_CHAT_SID);
        if (!sidPattern.matcher(regChatSid).matches())
        {
            this.setProperty(REG_CHAT_SID, "ABCG");
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Set deafault values if props does not exists
        ////////////////////////////////////////////////////////////////////////////////////////////

        /*
         * Stubs generated:
         *   cat ./src/main/java/ru/sincore/ConfigurationManager.java | grep 'public static final String' | grep -v 'HUB_CONFIG\|OP_CHAT_CID\|SECURITY_CID\|HUB_SID\|BOT_CHAT_SID' | awk '{printf("if (!this.containsKey(%s))\n{\n    this.setProperty(%s, \"\");\n}\n\n", $5, $5)}'
         */

		if (!this.containsKey(MIN_PASSWORD_LEN))
		{
			this.setProperty(MIN_PASSWORD_LEN,0);
		}

        if (!this.containsKey(MESSAGE_BUFFER_SIZE))
        {
            this.setProperty(MESSAGE_BUFFER_SIZE, 1024);
        }

        if (!this.containsKey(MESSAGE_BUFFER_FLUSH_PERIOD))
        {
            this.setProperty(MESSAGE_BUFFER_FLUSH_PERIOD, 500);
        }

        if (!this.containsKey(HUB_LISTEN))
        {
            this.setProperty(HUB_LISTEN, "0.0.0.0");
        }

        if (!this.containsKey(HUB_PORT))
        {
            this.setProperty(HUB_PORT, 411);
        }

        if (!this.containsKey(DISCONNECT_BY_TIMEOUT))
        {
            this.setProperty(DISCONNECT_BY_TIMEOUT, false);
        }

        if (!this.containsKey(MAX_KEEP_ALIVE_TIMEOUT))
        {
            this.setProperty(MAX_KEEP_ALIVE_TIMEOUT, 600);
        }

        if (!this.containsKey(BIG_FILE_STORAGE))
        {
            this.setProperty(BIG_FILE_STORAGE, 0);
        }

        if (!this.containsKey(FILE_STORAGE_LOCATION))
        {
            this.setProperty(FILE_STORAGE_LOCATION, "etc");
        }

        if (!this.containsKey(ABOUT_FILE))
        {
            this.setProperty(ABOUT_FILE, "about.%s.txt");
        }

        if (!this.containsKey(LICENSE_FILE))
        {
            this.setProperty(LICENSE_FILE, "license.%s.txt");
        }

        if (!this.containsKey(MOTD_FILE))
        {
            this.setProperty(MOTD_FILE, "motd.%s.txt");
        }

        if (!this.containsKey(RULES_FILE))
        {
            this.setProperty(RULES_FILE, "rules.%s.txt");
        }

        if (!this.containsKey(HUB_DEFAULT_LOCALE))
        {
            this.setProperty(HUB_DEFAULT_LOCALE, "en_US");
        }

        if (!this.containsKey(MAX_USERS))
        {
            this.setProperty(MAX_USERS, 1000);
        }

        if (!this.containsKey(USER_INITIAL_CAPACITY))
        {
            this.setProperty(USER_INITIAL_CAPACITY, 3000);
        }

        if (!this.containsKey(USER_LOAD_FACTOR))
        {
            this.setProperty(USER_LOAD_FACTOR, 0.75);
        }

        if (!this.containsKey(USER_CONNECTION_BUFFER_INITIAL_SIZE))
        {
            this.setProperty(USER_CONNECTION_BUFFER_INITIAL_SIZE, 1000);
        }

        if (!this.containsKey(MAX_HUBS_USERS))
        {
            this.setProperty(MAX_HUBS_USERS, 10);
        }

        if (!this.containsKey(MAX_HUBS_REGISTERED))
        {
            this.setProperty(MAX_HUBS_REGISTERED, 10);
        }

        if (!this.containsKey(MAX_SHARE_SIZE))
        {
            this.setProperty(MAX_SHARE_SIZE, 1000000);
        }

        if (!this.containsKey(MIN_SHARE_SIZE))
        {
            this.setProperty(MIN_SHARE_SIZE, 0);
        }

        if (!this.containsKey(MAX_NICK_SIZE))
        {
            this.setProperty(MAX_NICK_SIZE, 100);
        }

        if (!this.containsKey(MIN_NICK_SIZE))
        {
            this.setProperty(MIN_NICK_SIZE, 5);
        }

        if (!this.containsKey(MIN_SLOT_COUNT))
        {
            this.setProperty(MIN_SLOT_COUNT, 0);
        }

        if (!this.containsKey(MAX_SLOT_COUNT))
        {
            this.setProperty(MAX_SLOT_COUNT, 1000);
        }

        if (!this.containsKey(MAX_EMAIL_CHAR_COUNT))
        {
            this.setProperty(MAX_EMAIL_CHAR_COUNT, 250);
        }

        if (!this.containsKey(MAX_DESCRIPTION_CHAR_COUNT))
        {
            this.setProperty(MAX_DESCRIPTION_CHAR_COUNT, 1000);
        }

        if (!this.containsKey(MAX_OP_IN_HUB))
        {
            this.setProperty(MAX_OP_IN_HUB, 10);
        }

        if (!this.containsKey(MAX_CHAT_MESSAGE_SIZE))
        {
            this.setProperty(MAX_CHAT_MESSAGE_SIZE, 100000);
        }

        if (!this.containsKey(HUB_NAME))
        {
            this.setProperty(HUB_NAME, "HubName");
        }

        if (!this.containsKey(HUB_DESCRIPTION))
        {
            this.setProperty(HUB_DESCRIPTION, "HubDescription");
        }

        if (!this.containsKey(HUB_OWNER))
        {
            this.setProperty(HUB_OWNER, "root@localhost");
        }

        if (!this.containsKey(HUB_VERSION))
        {
            this.setProperty(HUB_VERSION, "0.0.1-development");
        }

        if (!this.containsKey(HUB_GREETING))
        {
            this.setProperty(HUB_GREETING, "Hi guy!");
        }

        if (!this.containsKey(NICK_CHAR))
        {
            this.setProperty(NICK_CHAR, "([\\\\w\\\\W]*)");
        }

        if (!this.containsKey(REDIRECT_URL))
        {
            this.setProperty(REDIRECT_URL, "adc://localhost:" + this.getInt(HUB_PORT));
        }

        if (!this.containsKey(OP_COMMAND_PREFIX))
        {
            this.setProperty(OP_COMMAND_PREFIX, "!");
        }

        if (!this.containsKey(USER_COMMAND_PREFIX))
        {
            this.setProperty(USER_COMMAND_PREFIX, "+");
        }

        if (!this.containsKey(MIN_CHARS_SEARCH_REQUEST))
        {
            this.setProperty(MIN_CHARS_SEARCH_REQUEST, 5);
        }

        if (!this.containsKey(MAX_CHARS_SEARCH_REQUEST))
        {
            this.setProperty(MAX_CHARS_SEARCH_REQUEST, 200);
        }

        if (!this.containsKey(SAVE_SEARCH_LOG))
        {
            this.setProperty(SAVE_SEARCH_LOG, true);
        }

        if (!this.containsKey(AUTOMATIC_SEARCH_INTERVAL))
        {
            this.setProperty(AUTOMATIC_SEARCH_INTERVAL, 36);
        }

        if (!this.containsKey(SEARCH_STEPS))
        {
            this.setProperty(SEARCH_STEPS, 6);
        }

        if (!this.containsKey(SEARCH_BASE_INTERVAL))
        {
            this.setProperty(SEARCH_BASE_INTERVAL, 2000);
        }

        if (!this.containsKey(SEARCH_SPAM_RESET))
        {
            this.setProperty(SEARCH_SPAM_RESET, 300);
        }

        if (!this.containsKey(HUB_LOGIN_TIMEOUT))
        {
            this.setProperty(HUB_LOGIN_TIMEOUT, 60);
        }

        if (!this.containsKey(KICK_DURATION))
        {
            this.setProperty(KICK_DURATION, 300);
        }

        if (!this.containsKey(HUB_MESSAGES_FILE_DIR))
        {
            this.setProperty(HUB_MESSAGES_FILE_DIR, hubConfigDir + "/clientmessages");
        }

        if (!this.containsKey(CHAT_REFRESH))
        {
            this.setProperty(CHAT_REFRESH, 10);
        }

        if (!this.containsKey(LAST_MESSAGES_COUNT))
        {
            this.setProperty(LAST_MESSAGES_COUNT, 10);
        }

        if (!this.containsKey(OP_CHAT_NAME))
        {
            this.setProperty(OP_CHAT_NAME, "OpChat");
        }

        if (!this.containsKey(OP_CHAT_DESCRIPTION))
        {
            this.setProperty(OP_CHAT_DESCRIPTION, "");
        }

        if (!this.containsKey(OP_CHAT_WEIGHT))
        {
            this.setProperty(OP_CHAT_WEIGHT, 0);
        }

        if (!this.containsKey(BOT_CHAT_NAME))
        {
            this.setProperty(BOT_CHAT_NAME, "BotChat");
        }

        if (!this.containsKey(BOT_CHAT_DESCRIPTION))
        {
            this.setProperty(BOT_CHAT_DESCRIPTION, "");
        }

        if (!this.containsKey(BOT_CHAT_WEIGHT))
        {
            this.setProperty(BOT_CHAT_WEIGHT, 0);
        }

        if (!this.containsKey(VIP_CHAT_NAME))
        {
            this.setProperty(VIP_CHAT_NAME, "VipChat");
        }

        if (!this.containsKey(VIP_CHAT_DESCRIPTION))
        {
            this.setProperty(VIP_CHAT_DESCRIPTION, "");
        }

        if (!this.containsKey(VIP_CHAT_WEIGHT))
        {
            this.setProperty(VIP_CHAT_WEIGHT, 0);
        }

        if (!this.containsKey(REG_CHAT_NAME))
        {
            this.setProperty(REG_CHAT_NAME, "RegChat");
        }

        if (!this.containsKey(REG_CHAT_DESCRIPTION))
        {
            this.setProperty(REG_CHAT_DESCRIPTION, "");
        }

        if (!this.containsKey(REG_CHAT_WEIGHT))
        {
            this.setProperty(REG_CHAT_WEIGHT, 0);
        }

        if (!this.containsKey(COMMAND_PM_RETURN))
        {
            this.setProperty(COMMAND_PM_RETURN, true);
        }

        if (!this.containsKey(MARK_REGISTRATION_ONLY))
        {
            this.setProperty(MARK_REGISTRATION_ONLY, true);
        }

        if (!this.containsKey(ADC_EXTENSION_LIST))
        {
            this.setProperty(ADC_EXTENSION_LIST, "");
        }

        if (!this.containsKey(ENABLE_ADCS))
        {
            this.setProperty(ENABLE_ADCS, false);
        }

        if (!this.containsKey(CERT_LOGIN))
        {
            this.setProperty(CERT_LOGIN, false);
        }

        if (!this.containsKey(USE_WORD_FILTER))
        {
            this.setProperty(USE_WORD_FILTER, true);
        }

        if (!this.containsKey(USE_WORD_FILTER_IN_PM))
        {
            this.setProperty(USE_WORD_FILTER_IN_PM, true);
        }
    }
}

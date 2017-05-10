<?php

// 'db_handler.php' has PHP code for connecting to the database
include ('../connection/db_handler.php');

// 'simple_html_dom.php' is a cusom php files, containing functions for parsing HTML
include ('../funcs/simple_html_dom.php');

// List of stopwords
$stopwords = array ("a", "as", "able", "about", "above", "according", "accordingly", "across", "actually", "after", "afterwards", "again", "against", "aint", "all", "allow", "allows", "almost", "alone", "along", "already", "also", "although", "always", "am", "among", "amongst", "an", "and", "another", "any", "anybody", "anyhow", "anyone", "anything", "anyway", "anyways", "anywhere", "apart", "appear", "appreciate", "appropriate", "are", "arent", "around", "as", "aside", "ask", "asking", "associated", "at", "available", "away", "awfully", "be", "became", "because", "become", "becomes", "becoming", "been", "before", "beforehand", "behind", "being", "believe", "below", "beside", "besides", "best", "better", "between", "beyond", "both", "brief", "but", "by", "cmon", "cs", "came", "can", "cant", "cannot", "cant", "cause", "causes", "certain", "certainly", "changes", "clearly", "co", "com", "come", "comes", "concerning", "consequently", "consider", "considering", "contain", "containing", "contains", "corresponding", "could", "couldnt", "course", "currently", "definitely", "described", "despite", "did", "didnt", "different", "do", "does", "doesnt", "doing", "dont", "done", "down", "downwards", "during", "each", "edu", "eg", "eight", "either", "else", "elsewhere", "enough", "entirely", "especially", "et", "etc", "even", "ever", "every", "everybody", "everyone", "everything", "everywhere", "ex", "exactly", "example", "except", "far", "few", "ff", "fifth", "first", "five", "followed", "following", "follows", "for", "former", "formerly", "forth", "four", "from", "further", "furthermore", "get", "gets", "getting", "given", "gives", "go", "goes", "going", "gone", "got", "gotten", "greetings", "had", "hadnt", "happens", "hardly", "has", "hasnt", "have", "havent", "having", "he", "hes", "hello", "help", "hence", "her", "here", "heres", "hereafter", "hereby", "herein", "hereupon", "hers", "herself", "hi", "him", "himself", "his", "hither", "hopefully", "how", "howbeit", "however", "i", "id", "ill", "im", "ive", "ie", "if", "ignored", "immediate", "in", "inasmuch", "inc", "indeed", "indicate", "indicated", "indicates", "inner", "insofar", "instead", "into", "inward", "is", "isnt", "it", "itd", "itll", "its", "its", "itself", "just", "keep", "keeps", "kept", "know", "knows", "known", "last", "lately", "later", "latter", "latterly", "least", "less", "lest", "let", "lets", "like", "liked", "likely", "little", "look", "looking", "looks", "ltd", "mainly", "many", "may", "maybe", "me", "mean", "meanwhile", "merely", "might", "more", "moreover", "most", "mostly", "much", "must", "my", "myself", "name", "namely", "nd", "near", "nearly", "necessary", "need", "needs", "neither", "never", "nevertheless", "new", "next", "nine", "no", "nobody", "non", "none", "noone", "nor", "normally", "not", "nothing", "novel", "now", "nowhere", "obviously", "of", "off", "often", "oh", "ok", "okay", "old", "on", "once", "one", "ones", "only", "onto", "or", "other", "others", "otherwise", "ought", "our", "ours", "ourselves", "out", "outside", "over", "overall", "own", "particular", "particularly", "per", "perhaps", "placed", "please", "plus", "possible", "presumably", "probably", "provides", "que", "quite", "qv", "rather", "rd", "re", "really", "reasonably", "regarding", "regardless", "regards", "relatively", "respectively", "right", "said", "same", "saw", "say", "saying", "says", "second", "secondly", "see", "seeing", "seem", "seemed", "seeming", "seems", "seen", "self", "selves", "sensible", "sent", "serious", "seriously", "seven", "several", "shall", "she", "should", "shouldnt", "since", "six", "so", "some", "somebody", "somehow", "someone", "something", "sometime", "sometimes", "somewhat", "somewhere", "soon", "sorry", "specified", "specify", "specifying", "still", "sub", "such", "sup", "sure", "ts", "take", "taken", "tell", "tends", "th", "than", "thank", "thanks", "thanx", "that", "thats", "thats", "the", "their", "theirs", "them", "themselves", "then", "thence", "there", "theres", "thereafter", "thereby", "therefore", "therein", "theres", "thereupon", "these", "they", "theyd", "theyll", "theyre", "theyve", "think", "third", "this", "thorough", "thoroughly", "those", "though", "three", "through", "throughout", "thru", "thus", "to", "together", "too", "took", "toward", "towards", "tried", "tries", "truly", "try", "trying", "twice", "two", "un", "under", "unfortunately", "unless", "unlikely", "until", "unto", "up", "upon", "us", "use", "used", "useful", "uses", "using", "usually", "value", "various", "very", "via", "viz", "vs", "want", "wants", "was", "wasnt", "way", "we", "wed", "well", "were", "weve", "welcome", "well", "went", "were", "werent", "what", "whats", "whatever", "when", "whence", "whenever", "where", "wheres", "whereafter", "whereas", "whereby", "wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who", "whos", "whoever", "whole", "whom", "whose", "why", "will", "willing", "wish", "with", "within", "without", "wont", "wonder", "would", "would", "wouldnt", "yes", "yet", "you", "youd", "youll", "youre", "youve", "your", "yours", "yourself", "yourselves", "zero");

// Function to retrieve all queries entered by users in a dataset of type array
function query_bank()
{
    // $connection is defined in 'db_handler.php'
    global $connection;
    
    // The SQL query toe execute in the database
    $sql = "SELECT * FROM queries ORDER BY query_count DESC";
    
    // Executing the SQL query and storing the result as a PDOStatement object in $result
    $result = $connection->query($sql);
    
    // If no results, then display error number and details
    if ($result == false)
        echo $connection->error;
    
    // Declaring array
    $bank = array();
    
    // Storing all queries into array $bank
    while ($row = $result->fetch_assoc())
    {
        // Array form [count, text, count, text...]
        array_push($bank, $row['query_count']);
        array_push($bank, $row['query_text']);
    }

    // Return the array $bank
    return $bank;
}

// Function to add the query to the database, and if existent, then just update count
function add_query()
{
    // $connection is defined in 'db_handler.php'
    global $connection;
    
    // Getting the query entered by user from URL
    $query = $_GET['query'];
    
    // The SQL query to execute
    $sql = "SELECT * FROM queries WHERE query_text = '$query'";

    // Executing the SQL query and storing the result as a PDOStatement object in $result 
    $result = $connection->query($sql);
    
    // If no results, then display error number and details
    if ($result == false)
        echo $connection->error;
    
    // Gettign the number of rows in $result
    $query_check = mysqli_num_rows($result);
    
    // If number of rows is not zero, then query is alread there, and so just increase count 
    if ($query_check != 0)
    {        
        $sql = "UPDATE queries 
        SET query_count = query_count + 1 
        WHERE query_text = '$query'";
        
        $result = $connection->query($sql);
        
        if ($result == false)
            echo $connection->error;
    }
    
    // If number of rows is zero otherwise, then query is not there, and so insert it, with count = 1
    else
    {
        $sql = "INSERT INTO queries (query_text, query_count)
        VALUES ('$query', 1)";
        
        $result = $connection->query($sql);
        
        if ($result == false)
            $result = $connection->query($sql);
    }
    
}

// Getting the search results as outputted by query processor from 'results' view
function get_results()
{
    // $connection is defined in 'db_handler.php'
    global $connection;
    
    // The SQL query to execute
    $sql = "SELECT * FROM results_view";
    
    // Executing the SQL query and storing the result as a PDOStatement in $result
    $result = $connection->query($sql);
    
    // If no results, then display error number and details
    if ($result == false)
        echo $connection->error;
    
    // Return the PDOStatement object $result
    return $result;
}

// Getting the description snippet as a string
function get_snippet($id, $query)
{
    // $connection is defined in 'db_handler.php'
    global $connection;
    
    //$result = "<b>Doc ID:</b> $id <br>";
    
    // Removing stop words from query entered by user
    //$query_no_stop = preg_replace("\b$stopwords\b", "", $query);
    
    // Storing the directory of the HTML doc as a string in variable $html_dir
    $html_dir = "../_crawled/$id.html";
    
    // Extracting the HTML code from HTML document
    $html_code = file_get_html($html_dir);
    
    // For each term in the tokenized string array query, without stopwords, search for it in the order
    //  of let's say <p> then <ul> then <table> and such. As soon as found, store the string
    
    // Search in that same string for the other word, if there, just highlight it as well, if not
    // then research using the same way
    
    // Keep doing so for the remaning terms
    
    // Finally, if after searcing for all terms you end up with one string, then it is the snippet, just
    //  truncate it to a certain number of words if it goes beyond a certain number with ... at beginning and/or end
    
    // If you ended up with multiple strings, then truncate them, placing ... in between and add them together
    
    // Extracting the <p> tag
    $title = $html_code->find('p', 0);
    
    // Getting the plaintext of the tag
    $result = $title->plaintext;
    
    // Return the string with the terms of the query entered by the user
    return $result;
}

// Sending the user entered query as a string to Java for processing, then getting the OK from Java
function send_query($query)
{
    // PHP as client; Java as server
    
    // Host is local host
    $address = "localhost";
    
    // The port to work on shall be 1235
    $service_port = 1235;
    
    // Creating a TCP/IP socket
    $socket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
    if ($socket === false) 
    {
        echo "socket_create() failed: reason: " . socket_strerror(socket_last_error()) . "\n";
    }
    
    // Connecting to specified address on specified port
    $result = socket_connect($socket, $address, $service_port);
    if ($result == false)
    {
        echo "socket_connect() failed.\nReason: ($result)". socket_strerror(socket_last_error($socket)) . "\n";
    }
    
    // Sending query string over port
    socket_write($socket, $query, strlen($query)) or die("Could not write query to port");
    
    // Closing the socket
    socket_close($socket);
    
    // Since the flush issue can't be solved here because keeping socket_read here will leave entire function hanging, thus not sending the string at first, and because writing socker_read in another function means a problem with closing the socket (which would flush the buffer), therefore the best solution was to make a socket for writing to Java and a socket for reading from Java on two different ports respectively
}

// Receiving response from Java server
function receive_response()
{
    // PHP as client; Java as server
    
    // Host is local host
    $address = "localhost";
    
    // The port to work on shall be 1235
    $service_port = 1236;
    
    // Creating a TCP/IP socket
    $socket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
    if ($socket === false) 
    {
        echo "socket_create() failed: reason: " . socket_strerror(socket_last_error()) . "\n";
    }
    
    // Connecting to specified address on specified port
    $result = socket_connect($socket, $address, $service_port);
    if ($result == false)
    {
        echo "socket_connect() failed.\nReason: ($result)". socket_strerror(socket_last_error($socket)) . "\n";
    }
    
    $out = '';
    
    // Enter while loop and keep reading from socket idefinitely
    while ($out = socket_read($socket, 1024))
    {
        // If nothing is read immediately, break
        if(!$out)
            break;
        
        // If the string read so far has '\n' in it, then get out of the loop (stop reading from socket)
        if(strpos($out, "\n") !== false)
            break;
    }
        
    // Closing the socket
    socket_close($socket);
    
    // Return the recieved response from port
    return $out;
}

?>
<?php

// 'db_handler.php' has PHP code for connecting to the database
include ('../connection/db_handler.php');

// 'simple_html_dom.php' is a cusom php files, containing functions for parsing HTML
include ('../funcs/simple_html_dom.php');

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

// Function to test the interface by changing/adding the VIEW 'results_view' to a desired form
function test_ui()
{
    // $connection is defined in 'db_handler.php'
    global $connection;
    
    // The SQL query to execute in the database to drop the VIEW 'results_view'
    $sql = "DROP VIEW results_view IF EXISTS";
    
    // Executing the query
    $connection->query($sql);
    
    // The SQL query to create VIEW 'results_view' with all the docs (desired form)
    $sql = "CREATE VIEW results_view AS SELECT * FROM doc_links";
    
    // Executing the query
    $connection->query($sql);
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

// Getting the search results as outputted by query processor from VIEW 'results_view'
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
    
    // Variable to store the description snippet
    $snippet = "";
    
    // Storing the directory of the HTML doc as a string in variable $html_dir
    $html_dir = "../_crawled/$id.html";
    
    // Extracting the HTML code as an object from HTML document in multiple variables, each whose name indicate what HTML code is there
    $html_code_all = file_get_html($html_dir);
    $html_code_no_title = file_get_html($html_dir);
    
    // Removing the desired tags from HTML code object. The HTML code object is passed by reference
    remove_tag($html_code_no_title, 'title');
    
    // Selecting (filtering) the desired tags. Since no tag number specified, all tags of that type are selected and added to an array, each tag number occupying a slot in the array as an object
    $head_no_title = $html_code_no_title->find('head');
    $body_no_title = $html_code_no_title->find('body');
    
    //echo $html_code_no_title->plaintext;
    
    // If $query is just a single word
    if (str_word_count($query) == 1)
    {
        // For each tag number object in the tag type's array $body_no_title
        foreach ($body_no_title as $tag_num)
        {
            // Plaintext of the current tag number
            $text = $tag_num->plaintext;

            // Splitting text into string array on space
            $text_arr = preg_split("/[\s]+/", $text);
            
            // If the query word is in that tag number (stripos() not strpos() as former is case insensitive)
            if (stripos($text, $query) == TRUE)
            {
                // Getting the first occurence of $query in the string, using 'true' to get all that precedes it (excluding occurence)
                $preceding = strstr($text, $query, true);
                
                // Getting the count of words preceting $query in the string 
                $count_preceding = str_word_count($preceding);
                
                // Getting the first occurence of $query in the string, using 'false' to get all that follows it (including occurence)
                $following = strstr($text, $query, false);
                
                // Getting the count of words following $query in the string
                $count_following = str_word_count($following);
                
                // Storing the position of $query in the string
                $position = $count_preceding + 1;
                             
                // Getting $query in the string
                $snippet = " " . $query . " ";
                
                // Getting 'n' words before $query in the string, concatenating it to $snippet
                $snippet = get_n_preceding($text_arr, $position, 20) . $snippet;
                
                // Getting 'n' words after $query in the string, concatenating it to $snippet
                $snippet = $snippet . get_n_following($text_arr, $position, 20);
                
                // Replacing the $query occurence with itself surrounded by bold tags (regex)
                $snippet = preg_replace("/$query/i", "<b>\$0</b>", $snippet);
            }
        }
    }
    
    // If $query is multiple words, and without quotations
    if (strlen($query) > 1 && strpos($query, '"') == FALSE)
    {
    }
    
    // If $query is multiple words, and with quotations
    if (strlen($query) > 1 && strpos($query, '"') == TRUE)
    {
    }

    // Extracting the <p> tag
    //$title = $html_code_all->find('title', 0);
    
    // Getting the plaintext of the tag
    //$result = $title->plaintext;

    // Return the string with the terms of the query entered by the user
    return $snippet;
}

// Function to remove the desired tag from HTML code object. '&' is used to pass the HTML code object by reference
function remove_tag(&$html_obj, $tag)
{
    // Filtering the desired tags associated with HTML code object, which would be stored in an array, each tag number occupying a slot in the array, stored as an object. NOTE, the find() operation acts like a filter as to what tags of the HTML code object will be visible, therefore, any change done to those found tags will directly affect the original HTML code object, because they are the both referencing the same thing - we merely just showed a filter of what's there
    $tag_type = $html_obj->find($tag);
    
    // Iterating over all the <title> tags
    foreach ($tag_type as $tag_info)
    {
        // Setting the outertext i.e. the tag itself to nothing
        $tag_info->outertext = '';
        
        // Setting the innertext i.e. the text of the tag itself to nothing. This is actually the plaintext of the tag but with HTML, thus innertext is used because we want to also remove tags that are acting as plaintext e.g. <title>Hello <b>World!</b></title> - plaintext is 'Hello World!' but innertext is 'Hello <b>World!</b>'
        $tag_info->innertext = '';    
    }
    
    // Alternative to setting the innertext to nothing is to the following,
    // ...saving
    //$save_state = $html_code_no_title->save();
    // ...and reloading
    //$html_code_no_title->load($save_state); 
}

// Function to get 'n' words before the current index, as a string
function get_n_preceding($arr, $index, $num)
{
    $string = "";

    // Getting 'n' words before current index
    for ($i = $index - 1; $i >= 0; $i--)
    {
        // Concatenate to the back
        $string = $arr[$i] . $string;
        $string = " " . $string;
                    
        // If 'n' words, then break (-1 to compensate starting index = 0)
        if ($index - $i == $num - 1)
        {
            // If start not reached, add '...'
            if ($i != 0)
                $string = "..." . $string;
            
            break;
        }
    }
    
    // Returning the 'n' words (string)
    return $string;
}  

// Function to get 'n' words after the current index, as a string
function get_n_following($arr, $index, $num)
{
    $string = "";
    
    // Getting 'n' words after the current index
    for ($i = $index + 1; $i <= count($arr); $i++)
    {
        // Concatenate to the front
        $string = $string . $arr[$i];
        $string = $string . " ";
                    
        // If 'n' words, then break (-1 to compensate starting index = 0)
        if ($i - $index == $num - 1)
        {
            // If end not reached, add '...'
            if ($i != count($arr))
                $string = $string . "...";
            
            break;
        }
    }
    
    // Returning the 'n' words (string)
    return $string;
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
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
    $sql = "SELECT * FROM results";
    
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
        
    // Variable to store the description snippet
    $snippet = "";
    
    // Storing the directory of the HTML doc as a string in variable $html_dir
    $html_dir = "../_crawler/HTMLs/$id.html";
    
    // Extracting the HTML code as an object from HTML document
    $html_code = file_get_html($html_dir);
    
    // Removing the desired tags from HTML code object. The HTML code object is passed by reference
    remove_tag($html_code, 'title');
    remove_tag($html_code, 'script');
    remove_tag($html_code, 'style');
        
    // Selecting (filtering) the desired tags. Since no tag number specified, all tags of that type are selected and added to an array, each tag number occupying a slot in the array as an object. Remember, <title>, <script> and <style> tags were removed previously, which justifies the term 'content'
    $html_content = $html_code->find('html');
        
    // If $query is just a single word or there are double quotations (phrase searching)
    if (str_word_count($query) == 1 or strpos($query, '"') !== FALSE)   // Somehow saying '== TRUE' instead doesn't work
    {        
        // If $query has double quotations, remove them
        if (strpos($query, '"') !== FALSE)
            $final_query = preg_replace("/\"/", "", $query);
        else
            $final_query = $query;
        
        // For each tag number of the particular tag type, in this case <html> tag. Usually, there is only one <html> in an HTML document, thus only $html_content[0], but foreach() used just in case there is more than one <html> in the HTMl document
        foreach ($html_content as $tag_num)
        {
            // Plaintext of the current tag number
            $text = $tag_num->plaintext;

            // Removing all kinds of special characters
            $text = preg_replace('/[^a-zA-Z0-9_ %\[\]\.\(\)%&-]/s', '', $text);
            
            // Cleaning the string from left and right because sometimes there can be many empty spaces (INDEXER ISSUE AS WELL MAYBE)
            $text = ltrim($text);
            $text = rtrim($text);
            
            // Using preg_match (regex) because strpos won't check each word alone, but also their subparts
            if (preg_match("/\b".preg_quote($final_query)."\b/i", $text))
            {                     
                // Getting the position of the first character of the query (stripos() not strpos() as former case insensitive)
                $queryStart = stripos($text, $final_query);
                
                // Getting the position of the last character of the query
                $queryEnd = $queryStart + strlen($final_query);
                                
                // Getting substring starting from number of characters desired before query, for the length of 100
                $before = get_n_preceding($text, $queryStart, 110);

                // Getting the query itself (starting from first character of query, for the length of the query)
                $at = substr($text, $queryStart, $queryEnd - $queryStart);
                
                // Getting the substring starting from right after query, for the length of 100
                $after = get_n_following($text, $queryEnd, 110);
  
                // Conctaneating into the snippet
                $snippet = $before . " " . $at . " " . $after;
                
                // Replacing the $query occurence with itself surrounded by bold tags (regex)...
                // ...preg_replace used to include all occurrences inconsiderate of capitalization
                $snippet = preg_replace("/$final_query/i", "<b>\$0</b>", $snippet);
                
                // No need to search in the remaning <html> tags, if there is more
                break;
            }
        }
    }
    
    // If $query is multiple words, and without quotations
    if (str_word_count($query) > 1 && strpos($query, '"') === FALSE)
    {                
        // Splitting the query entered by user ($query) into string array on space
        $query_arr = preg_split("/[\s]+/", $query);
        
        // For each tag number of the particular tag type, in this case <html> tag. Usually, there is only one <html> in an HTML document, thus only $html_content[0], but foreach() used just in case there is more than one <html> in the HTMl document
        foreach ($html_content as $tag_num)
        {
            // Plaintext of the current tag number
            $text = $tag_num->plaintext;

            // Removing all kinds of special characters
            $text = preg_replace('/[^a-zA-Z0-9_ %\[\]\.\(\)%&-]/s', '', $text);
            
            // Cleaning the string from left and right because sometimes there can be many empty spaces (INDEXER ISSUE AS WELL MAYBE)
            $text = ltrim($text);
            $text = rtrim($text);
            
            // For each word in $query_arr
            foreach ($query_arr as $query_word)
            {
                // Part of the snippet for each word in $query
                $snippet_part = "";
                
                // Using preg_match (regex) because strpos won't check each word alone, but also their subparts
                if (preg_match("/\b".preg_quote($query_word)."\b/i", $text))
                {   
                    // Getting the position of the first character of the query (stripos() not strpos() as former case insensitive)
                    $queryStart = stripos($text, $query_word);
                
                    // Getting the position of the last character of the query
                    $queryEnd = $queryStart + strlen($query_word);
                
                    // Getting substring starting from number of characters desired before query, for the length of 100
                    $before = get_n_preceding($text, $queryStart, 110);
                    
                    // Getting the query itself (starting from first character of query, for the length of the query)
                    $at = substr($text, $queryStart, $queryEnd - $queryStart);
                
                    // Getting the substring starting from right after query, for the length of 100
                    $after = get_n_following($text, $queryEnd, 110);

                    // Conctaneating into the snippet
                    $snippet_part = $before . " " . $at. " " . $after;
                    
                    // Replacing the $query occurence with itself surrounded by bold tags (regex)...
                    // ...preg_replace used to include all occurrences inconsiderate of capitalization
                    $snippet_part = preg_replace("/$query_word/i", "<b>\$0</b>", $snippet_part);
                }
                
                // If $snippet_part is not empty, a snippet text was found for the current term in the query word, and thus concatenate...
                if ($snippet_part != "")
                    // ...$snippet_part to $snippet, followed by "//" to indicate separation between each term's text snippet
                    $snippet = $snippet . $snippet_part . "// ";
            }
            
            // Trimming out the last '//'
            $snippet = rtrim($snippet, '// ');
        }
    }
    
    if ($snippet == "")
        $snippet = "<em>" . "Query's terms are either the in title of doc or in stemmed form within doc" . "</em>";

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

// Function to get 'n' words before the position index, as a string
function get_n_preceding($text, $index, $num)
{
    $string = "";
    
    // If index is greater than or equal to $num
    if ($index >= $num)
    {
        // Add ellipses at beginning
        $string = "...";
        
        // Concatenating substring that starts from number of characters $num before index, for the length of number of characters $num
        $string = $string . substr($text, $index - $num, $num);
    }
    else
        // No truncation, thus starting from 0, for the length of number of characters $num
        $string = substr($text, 0, $index);
    
    // Returning the 'n' words (string)
    return $string;
}  

// Function to get 'n' words after the position of index, as a string
function get_n_following($text, $index, $num)
{
    $string = "";
    
    // If what remains in $text starting from $index is greater than or equal to $num
    if (strlen($text) - $index >= $num)
    {   
        // Concatenating substring that starts from index till the number of character $num
        $string = substr($text, $index, $num);
                
        // Add ellipses at end
        $string = $string . "...";
    }
    else
        // No truncation, thus starting from index, for the length of number of remaining characters from index till end of textS
        $string = substr($text, $index, strlen($text) - $index);
    
    // Returning the 'n' words (string)
    return $string;
}

// Sending the user entered query as a string to Java for processing, then getting the OK from Java
function send_string_port($query, $port)
{
    // PHP as client; Java as server
    
    // Host is local host
    $address = "localhost";
    
    // The port to work on shall be 1235
    $service_port = $port;
    
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
function receive_string_port($port)
{
    // PHP as client; Java as server
    
    // Host is local host
    $address = "localhost";
    
    // The port to work on shall be 1235
    $service_port = $port;
    
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
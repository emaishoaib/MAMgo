<?php

// 'db_handler.php' has PHP code for connecting to the database
include 'db_handler.php';

// Function to retrieve all queries entered by users in a dataset of type array
function query_bank()
{
    global $connection;
    
    $sql = "SELECT * FROM queries ORDER BY query_count DESC";
    
    $result = $connection->query($sql);
    
    // If no results, then display error number and details
    if ($result == false)
        echo $connection->error;
    
    // Fetching all queries into array
    $bank = array();
    while ($row = $result->fetch_assoc())
    {
        //Array form [count, text, count, text...]
        array_push($bank, $row['query_count']);
        array_push($bank, $row['query_text']);
    }

    return $bank;
}

//Function to add the query to the database, and if existent, then just
//      update count
function add_query()
{
    global $connection;
    
    $query = $_GET['query'];
    
    $sql = "SELECT * FROM queries WHERE query_text = '$query'";
    
    $result = $connection->query($sql);
    
    if ($result == false)
        echo $connection->error;
    
    $query_check = mysqli_num_rows($result);
    if ($query_check != 0)
    {        
        $sql = "UPDATE queries 
        SET query_count = query_count + 1 
        WHERE query_text = '$query'";
        
        $result = $connection->query($sql);
        
        if ($result == false)
            echo $connection->error;
    }
    else
    {
        $sql = "INSERT INTO queries (query_text, query_count)
        VALUES ('$query', 1)";
        
        $result = $connection->query($sql);
        
        if ($result == false)
            $result = $connection->query($sql);
    }
    
}

// Getting the search results as outputted by query processor from
//      'results' view
function get_results()
{
    global $connection;
    
    $sql = "SELECT * FROM results_view";
    
    $result = $connection->query($sql);
    
    if ($result == false)
        echo $connection->error;
    
    return $result;
}

// Sending the user entered query as a string to Java for processing,
//      then getting the OK from Java
function send_query($query)
{
    //PHP as client; Java as server
    
    //Host is local host
    $address = "localhost";
    
    //The port to work on shall be 1235
    $service_port = 1235;
    
    //Creating a TCP/IP socket
    $socket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
    if ($socket === false) 
    {
        echo "socket_create() failed: reason: " . socket_strerror(socket_last_error()) . "\n";
    }
    
    //Connecting to specified address on specified port
    $result = socket_connect($socket, $address, $service_port);
    if ($result == false)
    {
        echo "socket_connect() failed.\nReason: ($result)". socket_strerror(socket_last_error($socket)) . "\n";
    }
    
    //Sending query string over port
    socket_write($socket, $query, strlen($query)) or die("Could not write query to port");
    
    //Closing the socket
    socket_close($socket);
    
    /*Since the flush issue can't be solved here because keeping socket_read
        here will leave entire function hanging, thus not sending the string
        at first, and because writing socker_read in another function means
        a problem with closing the socket (which would flush the buffer),
        therefore the best solution was to make a socket for writing to Java
        and a socket for reading from Java on two different ports respectively*/
}

// Receiving response from Java server
function receive_response()
{
    //PHP as client; Java as server
    
    //Host is local host
    $address = "localhost";
    
    //The port to work on shall be 1235
    $service_port = 1236;
    
    //Creating a TCP/IP socket
    $socket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
    if ($socket === false) 
    {
        echo "socket_create() failed: reason: " . socket_strerror(socket_last_error()) . "\n";
    }
    
    //Connecting to specified address on specified port
    $result = socket_connect($socket, $address, $service_port);
    if ($result == false)
    {
        echo "socket_connect() failed.\nReason: ($result)". socket_strerror(socket_last_error($socket)) . "\n";
    }
    
    $out = '';
    
    while ($out = socket_read($socket, 1024, PHP_NORMAL_READ))
    {
        if(!$out)
            break;
        
        if(strpos($out, "\n") !== false)
            break;
    }
        
    //Closing the socket
    socket_close($socket);
    
    return $out;
}

?>
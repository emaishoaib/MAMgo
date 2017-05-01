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
    
    // Freeing $result to be able to perform another fetch throughout controller.php
    //$result->close();
            
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

?>
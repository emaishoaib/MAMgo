<?php

// 'db_handler.php' has PHP code for connecting to the database
include 'db_handler.php';

// Function to retrieve all queries entered by users in a dataset of type array
function query_bank()
{
    global $connection;
    
    $sql = "SELECT * FROM query ORDER BY query_count DESC";
    
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
    $result->close();
            
    return $bank;
}

function insert_query()
{
}

function search()
{
}

?>
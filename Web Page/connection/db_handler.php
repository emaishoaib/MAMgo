<?php

// Connecting using connection varbiables set
$connection = mysqli_connect("localhost", "root", "", "search");

// If connecting failed
if (!$connection)
{
    // Get the connection error
    die("Connection failed: ".mysqli_connect_error());
}

?>
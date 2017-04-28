<?php
    // To fix the problem of header redirects
    ob_start();

    // For keeping and having the session variables throughout
    session_start();

    // Go to search.php
    header("Location: docs/search.php");
?>
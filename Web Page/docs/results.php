<?php
    // To fix the problem of header redirects
    ob_start();

    // For keeping and having the session variables throughout
    session_start();

    // 'controller.php' has all the functions needed, and also
    //      has 'db_handler.php' that connects to the database
    include ('../connection/controller.php');
?>

<!DOCTYPE html>

<html>
    
    <head>
        <!--Linking to CSS stylesheet-->
        <link href="../css/results.css" rel = "stylesheet">
        
        <!--Scripts needed for custom scripts to work-->
        <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/2.0.2/jquery.min.js"></script>
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
        <script src="../js/typeahead.bundle.js" type="text/javascript"></script> 
    </head>
    
    <body>
        
        <div id = "background">
            
            <div id = "header">
           
                <!--Custom JavaScript for enabling color gradient change-->
                <script src="../js/color-grad.js" type= "text/javascript"></script>

                    <div id = "logo">
                        <a href = "../index.php">
                            <h1>MAM</h1>
                            <h2>go</h2>
                        </a>
                    </div>

                <div id = "search">

                    <?php
                        //Update query list in DB
                        if (isset($_GET['query']))
                            add_query();
                    
                        //PHP for getting data set of all queries entered by users
                        $bank = query_bank();
                    ?>
                    
                    <script>
                        // Defining the dataset, encoding in JSON for Java variable
                        var bank = <?php echo json_encode($bank) ?>;                    
                    </script>

                    <!--Custom JavaScript based on Twitter's typeahead.js for 
                            suggestion mechanism; Bloodhound suggestion engine-->
                    <script src="../js/sugg.js" type = "text/javascript"></script>

                    <?php
                        if (isset($_GET['query']))
                        {
                            $query = $_GET['query'];
                        }
                    ?>
                    
                    <!--The input form to take the query from user. Class is set
                        as so based on Twitter's typeahead.js-->
                    <form method = "post">
                        <input type="text" name="search_bar" class ="typeahead tt-query" autocomplete="off" spellcheck="false" placeholder = "Search..." value="<?php echo $query?>">

                        <input type="image" src="../img/search-icon.png" class="search-btn" alt="Submit">
                    </form>

                    <!--Custom JavaScript enabling 'Enter' to search-->
                    <script>
                        $(".typeahead").keyup(function(event){
                            if(event.keyCode == 13)
                            {
                                $(".search-btn").click();
                            }
                        });
                    </script>

                    <!--Custom JavaScript fixing issue of two highlighed: by mouse and arrow keys simultaneously-->
                    <script>
                        $('body').on("mouseover", ".tt-suggestion", function () {
                            $('.tt-suggestion').removeClass('tt-cursor');
                            $(this).addClass('tt-cursor');
                        });
                    </script>
                    
                <?php                               
                    //If search done, then reload page with new query in URL
                    if (isset($_POST['search_bar']))
                    {
                        $location = "Location: results.php?query=";
                        $query = $_POST["search_bar"];
                        
                        $location .= $query;

                        header($location);
                    }
                ?>
                    
                </div>
                
            </div>
            
            <div id = "results">
                
                <!---Displaying search results-->
                
                <?php
                
                    //Getting the results
                    $result_records = get_results();
                    
                    if (mysqli_num_rows($result_records) == 0)
                    {
                ?>
                
                <!--Message conveying no searh results found-->
                <p>No results found</p>
                
                <?php
                    }
                    else
                    {
                        while ($row = $result_records->fetch_assoc())
                        {
                ?>
                
                <!--This part of HTML is repeated for each record (row) 
                        till all rows of 'results' view are retrieved-->
                <ul id = "result_record">
                    
                    <li id = "result_title">
                        <a href = "<?php echo $row['docLink']?>">
                            <?php echo $row['docTitle']?>
                        </a>
                    </li>
                    
                    <ul id = "result_details">
                        
                        <li id = "result_link">
                            <?php echo $row['docLink']?>
                        </li>
                        
                        
                        <li id = "result_description">
                            
                            <!--The following needs to be done
                                    1- Get HTML of resp. docID
                                    2- Parse only details of HTML
                                    3- Get string with all terms
                                    4- Display string (truncating and bolding terms)
                            -->
                            Description unavailable!
                        
                        </li>
                        
                    </ul>
                    
                </ul>
                
                <?php
                        }
                    }
                
                    //Dropping the 'results' view from DB
                    //drop_results();
                ?>
                
            </div>
        
        </div>
        
    </body>
</html>
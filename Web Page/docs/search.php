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
        <link href="../css/search.css" rel = "stylesheet">
        
        <!--jQuery-->
        <script src="http://code.jquery.com/jquery-latest.js"></script>>
        
    </head>
    
    <body>

        <div id = "background">
            
            <!--Custom JavaScript for enabling color gradient change-->
            <script src="../js/color-grad.js" type= "text/javascript"></script>
            
            <div id = "logo">
                <a href = "index.php">
                    <h1>MAM</h1>
                    <h2>go</h2>
                </a>
                <p></p>
            </div>
            
            <div id = "search">
                
                <!--PHP for getting data set of all queries entered by users-->
                <?php $bank = query_bank(); ?>
                
                <script>
                    // Defining the dataset, encoding in JSON for Java variable
                    var bank = <?php echo json_encode($bank) ?>;                    
                </script>
                
                <!--Custom JavaScript based on Twitter's typeahead.js for 
                        suggestion mechanism; Bloodhound suggestion engine.
                        The dataset is 'bank' declared above.-->
                <script src="../js/typeahead.bundle.js" type="text/javascript"></script> 
                <script src="../js/sugg.js" type = "text/javascript"></script>
                
                <!--The input form to take the query from user. Class is set
                        as so based on Twitter's typeahead.js-->
                <form method = "post">
                    <input type="text" class ="typeahead tt-query" name="searchBar" autocomplete="off" spellcheck="false" placeholder = "Search...">
                    
                    <input type="image" src="../img/search-icon.png" class="search_btn" alt="submit">
                </form>
                
                <!--Custom JavaScript enabling 'Enter' to search-->
                <script>
                    $(".typeahead").keyup(function(event){
                        if(event.keyCode == 13)
                        {
                            $(".search_btn").click();
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
                    //Go to 'results.php' with query text in URL
                    if (isset($_POST['searchBar']))
                    {
                        $location = "Location: results.php?query=";
                        $query = $_POST["searchBar"];
                        
                        $location .= $query;

                        header($location);
                        exit; //Very important, otherwise multiple directs
                    }
                ?>
                
            </div>

        </div>
        
    </body>
</html>
<?php
    // To fix the problem of header redirects
    ob_start();

    // For keeping and having the session variables throughout
    session_start();

    // 'controller.php' has all the functions needed, and also
    //      has 'db_handler.php' that connects to the database
    include ('connection/controller.php');
?>

<!DOCTYPE html>

<html>
    
    <head>
        <!--Linking to CSS stylesheet-->
        <link href="css/index.css" rel = "stylesheet">
        
        <!--Scripts needed for custom scripts to work-->
        <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/2.0.2/jquery.min.js"></script>
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
        <script src="js/typeahead.bundle.js" type="text/javascript"></script> 
    </head>
    
    <body>

        
        <div id = "background">
            
            <!--Custom JavaScript for enabling color gradient change-->
            <script src="js/color-grad.js" type= "text/javascript"></script>
            
            <div id = "logo">
                <h1>MAM</h1>
                <h2>go</h2>
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
                        suggestion mechanism; Bloodhound suggestion engine-->
                <script src="js/sugg.js" type = "text/javascript"></script>
                
                <!--The input form to take the query from user. Class is set
                        as so based on Twitter's typeahead.js-->
                <form method = "post">
                    <input type="text" name="search_bar" class ="typeahead tt-query" autocomplete="off" spellcheck="false" placeholder = "Search...">
                    
                    <input type="image" src="img/search-icon.png" class="search-btn" alt="Submit" >
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
        
    </body>
</html>
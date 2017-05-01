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
        
        <!--jQuery-->
        <script src="http://code.jquery.com/jquery-latest.js"></script>
        
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
                    <script src="../js/typeahead.bundle.js" type="text/javascript"></script> 
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
                ?>
                
                <ul class = "result_list">
                
                <?php
                        while ($row = $result_records->fetch_assoc())
                        {
                ?>
                
                    <!--This part of HTML is repeated for each record till 
                            till all rows of 'results' view are retrieved.-->
                    <ul class = "result_record">
                        
                        <li class = "result_title">
                            <a href = "<?php echo $row['docLink']?>">
                                <?php echo $row['docTitle']?>
                            </a>
                        </li>

                        <ul class = "result_details">

                            <li class = "result_link">
                                <?php echo $row['docLink']?>
                            </li>


                            <li class = "result_description">

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
                ?>
                
                </ul>
                
                <?php
                    }
                ?>
                
                <div id="pages">
                    <!--List of numbers for paginating, with each number being a
                            link controlled by the script that follows-->
                    <ul class="pagination">
                        <!--1, 2, 3...... will come here based on the script
                                that follows-->
                    </ul>
                </div>
                
                <!--Custom JavaScript for pagination using List.js-->
                <script src="../js/list.js"></script>
                
                <script>
                    var options = {
                        
                        //The class of the list containe. This is what
                        //  contains all the items (list) to be pagianted
                        listClass: "result_list",
                        
                        //The class of each list item; this class is within
                        //  the class of the list container. This is what
                        //  repeates itself many times, and is thus each
                        //  list item of the pagination
                        valueNames: ['result_record'],
                        
                        //Numebr of list items per page
                        page: 10,
                        
                        //Enable pagination
                        pagination: true
                    }
                    
                    //Creating the paginated list
                    var searchList = new List('results', options);
                </script>
                
                <script>
                    //Go to top of page when page link clicked (....)
                    $(".pagination").click(function() {
                        $('#results').scrollTop(0);
                    });
                </script>
                
            <!--(#results) div-->
            </div>
            
        <!--(#background) div-->
        </div>
        
    </body>
</html>
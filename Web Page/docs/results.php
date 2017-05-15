<?php
    // To fix the problem of header redirects
    ob_start();

    // For keeping and having the session variables throughout
    session_start();

    // 'controller.php' has all the functions needed, and also has 'db_handler.php' that connects to the database
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
           
                <!--Custom JavaScript for enabling color gradient change. Applied to selector (#logo h1) [check color-grad.js]-->
                <script src="../js/color-grad.js" type= "text/javascript"></script>

                <div id = "logo">
                    <a href = "../index.php">
                        <h1>MAM</h1>
                        <h2>go</h2>
                    </a>
                </div>

                <div id = "search">

                    <?php
                        // Update query list in DB, if parameter 'query' (GET) is set
                        if (isset($_GET['query']))
                            add_query();
                    
                        // PHP for getting all queries entered by users
                        $bank = query_bank();
                    ?>
                    
                    <script>
                        // Defining the dataset, encoding in JSON for Java variable
                        var bank = <?php echo json_encode($bank) ?>;                    
                    </script>

                    <!--Custom JavaScript based on Twitter's typeahead.js for suggestion mechanism; Bloodhound suggestion engine. The dataset is 'bank' declared above. [check sugg.js]-->
                    <script src="../js/typeahead.bundle.js" type="text/javascript"></script> 
                    <script src="../js/sugg.js" type = "text/javascript"></script>

                    <?php
                        // If the 'query' paramter is set in URL, store its value in $query
                        if (isset($_GET['query']))
                        {
                            $query = $_GET['query'];
                            
                            // Escaping any rogue string chars e.g. double quotations, thus displaying it when 'echo'
                            $display = htmlspecialchars($query, ENT_QUOTES, 'UTF-8');
                        }
                    ?>
                    
                    <!--The input form to take the query from user. Class is set as so based on Twitter's typeahead.js-->
                    <form id="search_bar" method = "post">
                        
                        <!--Class of text is set as so based on Twitter's typeahead.js. Value is set as so in order to display $query-->
                        <input type="text" class ="typeahead tt-query" name="searchBar" autocomplete="off" spellcheck="false" placeholder = "Search..." value="<?php echo $display?>">

                        <!--alt="submit" makes the png icon like a submit button, submitting the form and thus setting the POST-->
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
                    // If search done, then reload page with new query of searchBar in URL
                    if (isset($_POST['searchBar']))
                    {
                        // Preparing the location to go to
                        $location = "Location: results.php?query=";
                        
                        // Getting the query from search bar
                        $query = $_POST['searchBar'];
                        
                        // Concatenating query to location
                        $location = $location . $query;

                        // Header function may be slow
                        header($location);
                        
                        // To avoid multiple directs
                        exit;
                    }
                     
                    // Condition for the sake of testing interface without actual search. If there is 'test' parameter in the URL, then no actual search, just test. NOTE this test parameter can even be added through the search bar by typing the query, followed by '&test'
                    if (isset($_GET['test']))
                    {
                        // test_ui() is defined in 'controller.php'
                        test_ui();
                    }
                               
                    // If there is no 'test' parameter, then actual search
                    else
                    {
                        // Sending the query to Java for processing
                        send_query($query);
                               
                        // Receiving response from Java
                        $resp = receive_response();
                               
                        //echo $resp;
                    }
                ?>
                    
                </div>
                
            </div>
            
            <div id = "results">
                
                <?php
                
                    // Getting the results
                    $result_records = get_results();
                    
                    // If no results found, then...
                    if (mysqli_num_rows($result_records) == 0)
                    {
                ?>
                
                <!--Message conveying no searh results found-->
                <p>No results found</p>
                
                <?php
                    }
                
                    // If results found, then...
                    else
                    {
                ?>
                
                <!--Main unordered list that will be paginated-->
                <ul class = "result_list">
                
                <?php
                        // Iterate over each result record, and for each operate on the value of $row
                        while ($row = $result_records->fetch_assoc())
                        {
                ?>
                
                    <!--This part of HTML is repeated for each record till all rows of 'results' view are retrieved.-->
                    <ul class = "result_record">
                        
                        <!--Each result record is a row in the DB obetained from get_results(). Each record has a title with value of the current row's docTitle column in DB, and is a clickable link with value of the current row's docLink column in DB. There is also docID column for each record, but will be used later.-->
                        <li class = "result_title">
                            <a href = "<?php echo $row['docLink']?>">
                                <?php echo $row['docTitle']?>
                            </a>
                        </li>

                        <!--The details of each result record-->
                        <ul class = "result_details">

                            <!--Each result link just has a display value of current row's docLink column in DB-->
                            <li class = "result_link">
                                <?php echo $row['docLink']?>
                            </li>

                            <!--Each result description is...-->
                            <li class = "result_description">
                                
                                <?php
                                    // Getting the description snippet, by sending to 'get_snippet()':
                                    //      - The current record's ID with the value stored in the current row's docID
                                    //      - The query entered by the user found in the URL
                                    $snippet = get_snippet($row['docID'], $_GET['query']);
                                                                
                                    // Echoing the description snippet
                                    echo $snippet;
                                ?>
                                
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
                
                <!--Section to contain the pages links (pagination)-->
                <div id="pages">
                    
                    <!--List of numbers for paginating, with each number being a link controlled by the script that follows-->
                    <ul class="pagination">
                        
                        <!--1, 2, 3...... will come here automatically based on the script that follows-->
                        
                        <!--Custom JavaScript for pagination using List.js-->
                        <script src="../js/list.js"></script>

                        <!--Utilizing List.js for pagination-->
                        <script>
                            var options = {

                                // The class of the list container. This is what contains all the items (list) to be pagianted
                                listClass: "result_list",

                                // The class of each list item; this class is within the class of the list container. This is what repeates itself many times, and is thus each list item of the pagination
                                valueNames: ['result_record'],

                                // Numebr of list items per page
                                page: 10,

                                // Enable pagination
                                pagination: true
                            }

                            // Creating the paginated list
                            var searchList = new List('results', options);
                        </script>

                        <!--Script for going to top of page when page link is clicked-->
                        <script>
                            // (.pagination) is the selector to be clicked by user (page link)
                            $(".pagination").click(function() {

                                // (#background) is the destination, going to its top. Use when header is not scrolled along
                                $('#background').scrollTop(0);

                                // (#results) is the destination, going to its top. Use when header is scrolled along
                                //$('#results').scrollTop(0);
                            });
                        </script>
                        
                    </ul>
                    
                </div>
                
            </div>
            
        </div>
        
    </body>
</html>
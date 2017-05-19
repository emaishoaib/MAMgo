-- phpMyAdmin SQL Dump
-- version 4.5.1
-- http://www.phpmyadmin.net
--
-- Host: 127.0.0.1
-- Generation Time: May 16, 2017 at 02:05 AM
-- Server version: 10.1.19-MariaDB
-- PHP Version: 5.6.28

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `search`
--

-- --------------------------------------------------------

--
-- Table structure for table `doc_links`
--

CREATE TABLE `doc_links` (
  `docID` int(11) NOT NULL,
  `docTitle` varchar(255) DEFAULT '[In Progress]',
  `docLink` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `pos_index`
--

CREATE TABLE `pos_index` (
  `term` varchar(255) NOT NULL,
  `docID` int(11) NOT NULL,
  `tagNum` int(11) NOT NULL,
  `posNum` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `queries`
--

CREATE TABLE `queries` (
  `query_text` varchar(255) NOT NULL,
  `query_count` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `queries`
--

INSERT INTO `queries` (`query_text`, `query_count`) VALUES
('Are rhinos really extinct?', 13),
('How much is Bill Gate''s salary?', 2),
('Where is Bill Gates from?', 5),
('Where is Jack?', 1),
('Where is Jennifer Lawrence from?', 2),
('Where is Martian from?', 2),
('Where is Mustafa Shoaib from?', 1),
('Where is Nicole Kidman from?', 1),
('Where is Snowden from?', 65),
('Where is Steve Jobs from?', 10),
('Who is the wife of Bill Gates?', 23),
('Who is the wife of Brad Pitt?', 100);

-- --------------------------------------------------------

--
-- Stand-in structure for view `results_view`
--
CREATE TABLE `results_view` (
`docID` int(11)
,`docLink` varchar(255)
,`docTitle` varchar(255)
);

-- --------------------------------------------------------

--
-- Table structure for table `tag_index`
--

CREATE TABLE `tag_index` (
  `term` varchar(255) NOT NULL,
  `docID` int(11) NOT NULL,
  `titleTag` int(11) DEFAULT '0',
  `hTag` int(11) DEFAULT '0',
  `boldTag` int(11) DEFAULT '0',
  `italicTag` int(11) DEFAULT '0',
  `contentTag` int(11) DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Structure for view `results_view`
--
DROP TABLE IF EXISTS `results_view`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `results_view`  AS  select distinct `a`.`docID` AS `docID`,`b`.`docLink` AS `docLink`,`b`.`docTitle` AS `docTitle` from (`pos_index` `a` join `doc_links` `b`) where ((`a`.`term` = 'apples') and (`a`.`docID` = `b`.`docID`)) union select distinct `a`.`docID` AS `docID`,`b`.`docLink` AS `docLink`,`b`.`docTitle` AS `docTitle` from (`pos_index` `a` join `doc_links` `b`) where ((`a`.`term` = 'appl') and (`a`.`docID` = `b`.`docID`)) order by `docID` ;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `doc_links`
--
ALTER TABLE `doc_links`
  ADD PRIMARY KEY (`docID`);

--
-- Indexes for table `pos_index`
--
ALTER TABLE `pos_index`
  ADD PRIMARY KEY (`term`,`docID`,`tagNum`,`posNum`);

--
-- Indexes for table `queries`
--
ALTER TABLE `queries`
  ADD PRIMARY KEY (`query_text`);

--
-- Indexes for table `tag_index`
--
ALTER TABLE `tag_index`
  ADD PRIMARY KEY (`term`,`docID`),
  ADD KEY `docID` (`docID`);

--
-- Constraints for dumped tables
--

--
-- Constraints for table `pos_index`
--
ALTER TABLE `pos_index`
  ADD CONSTRAINT `pos_index_ibfk_1` FOREIGN KEY (`term`) REFERENCES `tag_index` (`term`) ON DELETE CASCADE;

--
-- Constraints for table `tag_index`
--
ALTER TABLE `tag_index`
  ADD CONSTRAINT `tag_index_ibfk_1` FOREIGN KEY (`docID`) REFERENCES `doc_links` (`docID`) ON DELETE CASCADE;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
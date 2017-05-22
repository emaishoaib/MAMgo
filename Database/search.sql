-- phpMyAdmin SQL Dump
-- version 4.5.1
-- http://www.phpmyadmin.net
--
-- Host: 127.0.0.1
-- Generation Time: May 22, 2017 at 03:47 PM
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
-- Table structure for table `doc_hits`
--

CREATE TABLE `doc_hits` (
  `docLink` varchar(600) NOT NULL,
  `docHits` int(11) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `doc_links`
--

CREATE TABLE `doc_links` (
  `docID` int(11) NOT NULL,
  `docTitle` varchar(255) DEFAULT 'Non-Available',
  `docLink` varchar(255) NOT NULL,
  `docWordCount` int(11) NOT NULL DEFAULT '0',
  `isIndexed` int(11) NOT NULL DEFAULT '1'
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

-- --------------------------------------------------------

--
-- Table structure for table `results`
--

CREATE TABLE `results` (
  `docRank` int(11) NOT NULL DEFAULT '1',
  `docID` int(11) NOT NULL,
  `docTitle` varchar(255) NOT NULL DEFAULT 'Non Available',
  `docLink` varchar(255) DEFAULT NULL,
  `docHits` int(11) NOT NULL DEFAULT '1',
  `docWordCount` int(11) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Stand-in structure for view `results_view`
--
CREATE TABLE `results_view` (
`docID` int(11)
,`docTitle` varchar(255)
,`docLink` varchar(255)
,`docWordCount` int(11)
,`isIndexed` int(11)
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

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `results_view`  AS  select `doc_links`.`docID` AS `docID`,`doc_links`.`docTitle` AS `docTitle`,`doc_links`.`docLink` AS `docLink`,`doc_links`.`docWordCount` AS `docWordCount`,`doc_links`.`isIndexed` AS `isIndexed` from `doc_links` ;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `doc_hits`
--
ALTER TABLE `doc_hits`
  ADD PRIMARY KEY (`docLink`);

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
-- Indexes for table `results`
--
ALTER TABLE `results`
  ADD PRIMARY KEY (`docID`);

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

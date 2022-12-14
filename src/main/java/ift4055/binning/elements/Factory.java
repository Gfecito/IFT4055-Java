package ift4055.binning.elements;

import ift4055.assemblyGraph.Graph;

/**
 * Factories for our data elements
 */
public interface Factory {

    interface Group extends Factory{
        /**
         *
         * @param nMembers the amount of contig members in this group.
         * @return the group with space for its contigs.
         */
        Element.Group newGroup(int nMembers);
    }
    interface Segment extends Factory{
        /**
         *
         * @return a segment taken from the free segment chain.
         */
        Element.Segment newSegment();

        /**
         *
         * @return non-null segments in factory (the ones with parents)
         */
        Element.Segment[] getSegments();
    }
    interface Match extends Factory{
        /**
         *
         * @param strand DNA sequence strand.
         * @param rMin lowest read position.
         * @param span span of match.
         * @param wMin starting writing position.
         * @param dnaSequence a sequence of nucleotides which might go further than just this match. Like a stream.
         *                    If not, just set offset to 0.
         * @param offset the start of the encoded DNA sequence of this match
         *                    (the sequence that is overlapped with another segment).
         * @return the match with these characteristics.
         */
        Element.Match newMatch(int strand, int rMin, int span, int wMin, byte[] dnaSequence, int offset);


        /**
         *
         * @return non-null matches in factory
         */
        Element.Match[] getMatches();
    }
    interface Syndrome extends Factory{
        /**
         *
         * @param syndrome strand encoding.
         * @param readPosition the position that corresponds to this match's syndromes.
         * @return the syndrome with its strand encoding and read position.
         */
        Element.Syndrome newSyndrome(int syndrome, int readPosition);

        /**
         *
         * @return decoded non-null syndromes in this factory.
         */
        Element.Syndrome[] getSyndromes();
    }
    interface Insert extends Factory{
        /**
         *
         * @param strand DNA sequence strand.
         * @param rMin lowest read position.
         * @param span span of insert.
         * @param wMin starting writing position.
         * @param dnaSequence a sequence of nucleotides which might go further than just this insert. Like a stream.
         *                    If not, just set offset to 0.
         * @param offset the start of the encoded DNA sequence of this insert.
         * @return the insert with these characteristics.
         */
        Element.Insert newInsert(int strand, int rMin, int span, int wMin, byte[] dnaSequence, int offset);

        /**
         *
         * @return non-null inserts in factory
         */
        Element.Insert[] getInserts();
    }
    interface Base extends Factory{
        /**
         *
         * @param b the integer representing the base, to be compressed in its factory.
         * @return the base, it basically just returns b wrapped.
         */
        Element.Base addBase(int b);

        /**
         *
         * @return an array of all the encoded/compressed bases in this factory
         */
        Element.Base[] getBases();
    }


    /* GRAPH */
    interface Edge extends Factory{
        /**
         *
         * @param i the start of the edge.
         * @param sigma the span of the edge.
         * @return the edge.
         */
        Graph.Edge newEdge(int i, int sigma);

        /**
         *
         * @return non-null edges in factory
         */
        Graph.Edge[] getEdges();
    }
    interface Node extends Factory{
        /**
         *
         * @return a new node.
         */
        Graph.Node newNode();

        /**
         *
         * @return non-null nodes in factory (the ones with parents)
         */
        Graph.Node[] getNodes();
    }
    interface Connector extends Factory{
        /**
         *
         * @return a new connector.
         */
        Graph.Connector newConnector();

        /**
         *
         * @return non-null connectors in factory (the ones with sources)
         */
        Graph.Connector[] getConnectors();
    }
}

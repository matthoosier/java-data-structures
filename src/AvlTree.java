import java.util.HashMap;

// AvlTree class
//
// CONSTRUCTION: with no initializer
//
// ******************PUBLIC OPERATIONS*********************
// void insert( x )       --> Insert x
// void remove( x )       --> Remove x (unimplemented)
// boolean contains( x )  --> Return true if x is present
// Comparable findMin( )  --> Return smallest item
// Comparable findMax( )  --> Return largest item
// boolean isEmpty( )     --> Return true if empty; else false
// void makeEmpty( )      --> Remove all items
// void printTree( )      --> Print tree in sorted order
// ******************ERRORS********************************
// Throws UnderflowException as appropriate

/**
 * Implements an AVL tree.
 * Note that all "matching" is based on the compareTo method.
 * @author Mark Allen Weiss
 */
public class AvlTree<AnyType extends Comparable<? super AnyType>>
{
    /**
     * Construct the tree.
     */
    public AvlTree( )
    {
        root = null;
    }

    /**
     * Insert into the tree; duplicates are ignored.
     * @param x the item to insert.
     */
    public void insert( AnyType x )
    {
        root = insert( x, root );
    }

    /**
     * Remove from the tree. Nothing is done if x is not found.
     * @param x the item to remove.
     */
    public void remove( AnyType x )
    {
    	root = remove( x, root );
    }

    /**
     * Find the smallest item in the tree.
     * @return smallest item or null if empty.
     */
    public AnyType findMin( )
    {
        if( isEmpty( ) )
            throw new UnderflowException( );
        return findMin( root ).element;
    }

    /**
     * Find the largest item in the tree.
     * @return the largest item of null if empty.
     */
    public AnyType findMax( )
    {
        if( isEmpty( ) )
            throw new UnderflowException( );
        return findMax( root ).element;
    }

    /**
     * Find an item in the tree.
     * @param x the item to search for.
     * @return true if x is found.
     */
    public boolean contains( AnyType x )
    {
        return contains( x, root );
    }

    /**
     * Make the tree logically empty.
     */
    public void makeEmpty( )
    {
        root = null;
    }

    /**
     * Test if the tree is logically empty.
     * @return true if empty, false otherwise.
     */
    public boolean isEmpty( )
    {
        return root == null;
    }

    /**
     * Print the tree contents in sorted order.
     */
    public void printTree( )
    {
        if( isEmpty( ) )
            System.out.println( "Empty tree" );
        else
        	internalInOrderTraversal( root, new NodeOperator<AnyType>() {

				public void operate(AvlNode<AnyType> node)
				{
					System.out.println( node.element );
				}
        		
        	});
    }
    
    @SuppressWarnings("unused")
	private void enforceBalanceOld( AvlNode<AnyType> node )
    {
    	internalInOrderTraversal( node, new NodeOperator<AnyType> () {

			public void operate(AvlNode<AnyType> node)
			{
				assert( height(node.left) - height(node.right) <= 1 );
				assert( height(node.right) - height(node.left) <= 1 );
				
				if( node.left != null ) {
					assert( node.element.compareTo( node.left.element ) > 0 );
					
				}
				
				if( node.right != null ) {
					assert( node.element.compareTo( node.right.element ) < 0 );
				}
			}
		});
    }
    
    /**
     * Internal method to insert into a subtree.
     * @param x the item to insert.
     * @param t the node that roots the subtree.
     * @return the new root of the subtree.
     */
    private AvlNode<AnyType> insert( AnyType x, AvlNode<AnyType> t )
    {
        if( t == null )
            return new AvlNode<AnyType>( x, null, null );
        
        int compareResult = x.compareTo( t.element );
        
        if( compareResult < 0 )
        {
            t.left = insert( x, t.left );
            t = rebalance( t, RebalanceOption.FORBID_EQUAL_SUBTREE_HEIGHTS );
        }
        else if( compareResult > 0 )
        {
            t.right = insert( x, t.right );
            t = rebalance( t, RebalanceOption.FORBID_EQUAL_SUBTREE_HEIGHTS );
        }
        else
            ;  // Duplicate; do nothing

        return t;
    }
    
    private AvlNode<AnyType> remove( AnyType x, AvlNode<AnyType> t )
    {
    	if( t == null )
    		return null;
    	
    	int compareResult = x.compareTo( t.element );
    	
    	if( compareResult < 0 )
    	{
    		t.left = remove( x, t.left );
    		t = rebalance( t, RebalanceOption.ALLOW_EQUAL_SUBTREE_HEIGHTS );
    	}
    	else if( compareResult > 0 )
    	{
    		t.right = remove( x, t.right );
    		t = rebalance( t, RebalanceOption.ALLOW_EQUAL_SUBTREE_HEIGHTS );
    	}
    	else
    	{
    		// This node is the element.

    		if( t.left == null && t.right == null )
    		{
    			// Easy case: already a leaf
    			t = null;
    		}
    		else if( t.left == null && t.right != null )
    		{
    			// No left child. Just promote right.
    			t = t.right;
    		}
    		else if( t.left != null && t.right == null )
    		{
    			// No right child. Just promote left.
    			t = t.left;
    		}
    		else
    		{
    			// Both subtrees nonempty.
    			AvlNode<AnyType> right = t.right;
    			RemoveMaxResult<AnyType> leftWithoutMax = removeMax( t.left );

    			t = leftWithoutMax.maximum;
    			t.left = leftWithoutMax.remainingTree;
    			t.right = right;
    			t.height = Math.max( height( t.left ), height( t.right ) ) + 1;
    			
    			t = rebalance( t, RebalanceOption.ALLOW_EQUAL_SUBTREE_HEIGHTS );
    		}
    	}

    	return t;
    }
    
    private RemoveMaxResult<AnyType> removeMax( AvlNode<AnyType> t )
    {
		// No right subtree, so this root is the max element
    	if( t.right == null )
    	{
    		// A left subtree exists. Unlink the root and return the left as the remaining tree.
    		if( t.left != null )
    		{
        		AvlNode<AnyType> left = t.left;
        		t.left = null;
        		return new RemoveMaxResult<AnyType>( left, t );
    		}
    		// No left exists. Just unlink the root.
    		else
    		{
    			return new RemoveMaxResult<AnyType>( null, t );
    		}
    	}
    	// Max is somewhere on the right subtree
    	else
    	{
    		RemoveMaxResult<AnyType> shortenedRight = removeMax( t.right );
    		
    		t.right = shortenedRight.remainingTree;
    		
    		t = rebalance( t, RebalanceOption.ALLOW_EQUAL_SUBTREE_HEIGHTS );
    		
    		return new RemoveMaxResult<AnyType>( t, shortenedRight.maximum );
    	}
    }
    
    private static class RemoveMaxResult<AnyType>
    {
    	public RemoveMaxResult( AvlNode<AnyType> remainingTree, AvlNode<AnyType> maximum )
    	{
    		this.remainingTree = remainingTree;
    		this.maximum = maximum;
    	}
    	
    	private AvlNode<AnyType> remainingTree;
    	private AvlNode<AnyType> maximum;
    }
    
    private enum RebalanceOption
    {
    	ALLOW_EQUAL_SUBTREE_HEIGHTS,
    	FORBID_EQUAL_SUBTREE_HEIGHTS,
    }
    
    private AvlNode<AnyType> rebalance( AvlNode<AnyType> t, RebalanceOption option )
    {
        if( height( t.left ) - height( t.right ) > 1 )
        {
        	// Check that imbalance never got larger than 2
        	assert( height( t.left ) - height( t.right ) == 2 );
        	
            if( height( t.left.left ) > height( t.left.right ) )
            {
            	// Out of balance in straight line to the outside
                t = rotateWithLeftChild( t );
            }
            else if( height( t.left.right ) > height( t.left.left ) )
            {
            	// Out of balance bending back toward the inside
                t = doubleWithLeftChild( t );
            }
            else if( option == RebalanceOption.ALLOW_EQUAL_SUBTREE_HEIGHTS )
            {
            	// Left subtree's children are same height. This only happens
            	// during removal. A single rotation is the cure here.
            	t = rotateWithLeftChild( t );
            }
            else
            {
            	// Both sub-subtrees can't have grown taller during a single insertion
            	assert( false );
            }
        }
        else if( height( t.right ) - height( t.left ) > 1 )
        {
        	// Check that imbalance never got larger than 2
        	assert( height( t.right ) - height( t.left ) == 2 );
        	
            if( height( t.right.right ) > height( t.right.left ) )
            {
            	// Out of balance in straight line to the outside
                t = rotateWithRightChild( t );
            }
            else if( height( t.right.left ) > height( t.right.right ) )
            {
            	// Out of balance bending back toward the inside
                t = doubleWithRightChild( t );
            }
            else if( option == RebalanceOption.ALLOW_EQUAL_SUBTREE_HEIGHTS )
            {
            	// Right subtree's children are same height. This only happens
            	// during removal. A single rotation is the cure here.
            	t = rotateWithRightChild( t );
            }
            else
            {
            	// Both sub-subtrees can't have grown taller during a single insertion
            	assert( false );
            }
        }
        t.height = Math.max( height( t.left ), height( t.right ) ) + 1;
        
        return t;
    }

    /**
     * Internal method to find the smallest item in a subtree.
     * @param t the node that roots the tree.
     * @return node containing the smallest item.
     */
    private AvlNode<AnyType> findMin( AvlNode<AnyType> t )
    {
        if( t == null )
            return t;

        while( t.left != null )
            t = t.left;
        return t;
    }

    /**
     * Internal method to find the largest item in a subtree.
     * @param t the node that roots the tree.
     * @return node containing the largest item.
     */
    private AvlNode<AnyType> findMax( AvlNode<AnyType> t )
    {
        if( t == null )
            return t;

        while( t.right != null )
            t = t.right;
        return t;
    }

    /**
     * Internal method to find an item in a subtree.
     * @param x is item to search for.
     * @param t the node that roots the tree.
     * @return true if x is found in subtree.
     */
    private boolean contains( AnyType x, AvlNode<AnyType> t )
    {
        while( t != null )
        {
            int compareResult = x.compareTo( t.element );
            
            if( compareResult < 0 )
                t = t.left;
            else if( compareResult > 0 )
                t = t.right;
            else
                return true;    // Match
        }

        return false;   // No match
    }
    
    /**
     * Internal method for in-order traversal.
     * @param t the node that roots the tree
     * @param op the action to take on each node
     */
    private void internalInOrderTraversal( AvlNode<AnyType> t, NodeOperator<AnyType> op )
    {
    	if( t != null )
    	{
    		internalInOrderTraversal( t.left, op );
    		op.operate( t );
    		internalInOrderTraversal( t.right, op );
    	}
    }
    
    /**
     * Return the height of node t, or -1, if null.
     */
    private int height( AvlNode<AnyType> t )
    {
        return t == null ? -1 : t.height;
    }

    /**
     * Rotate binary tree node with left child.
     * For AVL trees, this is a single rotation for case 1.
     * Update heights, then return new root.
     */
    private AvlNode<AnyType> rotateWithLeftChild( AvlNode<AnyType> k2 )
    {
        AvlNode<AnyType> k1 = k2.left;
        k2.left = k1.right;
        k1.right = k2;
        k2.height = Math.max( height( k2.left ), height( k2.right ) ) + 1;
        k1.height = Math.max( height( k1.left ), k2.height ) + 1;
        return k1;
    }

    /**
     * Rotate binary tree node with right child.
     * For AVL trees, this is a single rotation for case 4.
     * Update heights, then return new root.
     */
    private AvlNode<AnyType> rotateWithRightChild( AvlNode<AnyType> k1 )
    {
        AvlNode<AnyType> k2 = k1.right;
        k1.right = k2.left;
        k2.left = k1;
        k1.height = Math.max( height( k1.left ), height( k1.right ) ) + 1;
        k2.height = Math.max( height( k2.right ), k1.height ) + 1;
        return k2;
    }

    /**
     * Double rotate binary tree node: first left child
     * with its right child; then node k3 with new left child.
     * For AVL trees, this is a double rotation for case 2.
     * Update heights, then return new root.
     */
    private AvlNode<AnyType> doubleWithLeftChild( AvlNode<AnyType> k3 )
    {
        k3.left = rotateWithRightChild( k3.left );
        return rotateWithLeftChild( k3 );
    }

    /**
     * Double rotate binary tree node: first right child
     * with its left child; then node k1 with new right child.
     * For AVL trees, this is a double rotation for case 3.
     * Update heights, then return new root.
     */
    private AvlNode<AnyType> doubleWithRightChild( AvlNode<AnyType> k1 )
    {
        k1.right = rotateWithLeftChild( k1.right );
        return rotateWithRightChild( k1 );
    }

    private static class AvlNode<AnyType>
    {
            // Constructors
		AvlNode( AnyType theElement )
        {
            this( theElement, null, null );
        }

        AvlNode( AnyType theElement, AvlNode<AnyType> lt, AvlNode<AnyType> rt )
        {
            element  = theElement;
            left     = lt;
            right    = rt;
            height   = 0;
        }
        
        public String toString( )
        {
        	return "AvlNode (" + element.toString() + ")";
        }
        
        public AvlNode<AnyType> clone( HashMap<AvlNode<AnyType>, AvlNode<AnyType>> cloneMap)
        {
        	if( cloneMap.containsKey( this ) )
        	{
        		return cloneMap.get( this );
        	}
        	else
        	{
        		AvlNode<AnyType> clone = new AvlNode<AnyType>( this.element );
        		cloneMap.put( this, clone );
        		
        		if( left != null )
        		{
        			clone.left = left.clone( cloneMap );
        		}
        		
        		if( right != null )
        		{
        			clone.right = right.clone( cloneMap );
        		}
        		
        		clone.height = height;
        		return clone;
        	}
        }

        AnyType           element;      // The data in the node
        AvlNode<AnyType>  left;         // Left child
        AvlNode<AnyType>  right;        // Right child
        int               height;       // Height
    }
    
    private static interface NodeOperator<AnyType extends Comparable<? super AnyType>>
    {
    	void operate( AvlNode<AnyType> node );
    }

      /** The tree root. */
    private AvlNode<AnyType> root;
}

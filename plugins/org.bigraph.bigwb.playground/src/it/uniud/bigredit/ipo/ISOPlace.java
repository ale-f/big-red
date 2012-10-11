package it.uniud.bigredit.ipo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.Layoutable;
import org.bigraph.model.Root;
import org.bigraph.model.Site;

public class ISOPlace {

	private String BIGRAPH_B0 = "bigraph_b0";
	private String BIGRAPH_B1 = "bigraph_b1";
	private Bigraph b0, b1;

	private HashMap<String, SharedObject> sharedTable = new HashMap<String, SharedObject>();
	private List<Layoutable> c0, c1;
	private List<SharedObject> v2;
	private HashMap<Root, Integer> interfaceMap = new HashMap<Root, Integer>();
	private HashMap<Layoutable, List<Layoutable>> parentMapBig0 = new HashMap<Layoutable, List<Layoutable>>();
	private HashMap<Layoutable, List<Layoutable>> parentMapBig1 = new HashMap<Layoutable, List<Layoutable>>();

	private Bigraph bigc0;
	private Bigraph bigc1;

	public void analize(Bigraph b0, Bigraph b1) {
		reset();
		
		this.b0 = b0;
		this.b1 = b1;
		addBigraph(b0, BIGRAPH_B0);
		addBigraph(b1, BIGRAPH_B1);

		boolean consistent = check_consistency();
		if (!consistent) {
			System.out
					.println("the bigraph selected don't respect the consistency properties");
			return;
		}
		fillUnsharedList();
		createInterfacePlaceList();
		createPushoutPlaceC0();
		createPushoutPlaceC1();
		printPushout();
	}

	private void createPushoutPlaceC0() {
		int nSite;
		for (Root root : b0.getRoots()) {
			Site site = new Site();
			if (interfaceMap.containsKey(root)) {
				/** TODO add site */
				
				addNodetoParentMap(root, site, BIGRAPH_B0);
			} else {
				Layoutable parent = null;
				for (Layoutable child : root.getChildren()) {
					parent = sharedTable.get(child.getName()).getParentB1();
				}
				if (parent != null) {
					/** TODO add site */
					addNodetoParentMap(parent, site, BIGRAPH_B0);
				}
			}
		}
		for (Layoutable node : c0) {
			Layoutable parent = node.getParent();

			if (parent instanceof Root) {

				/** TODO add this to child of interfaceMap.containsKey(root) **/
				//addNodetoParentMap(parent, (Layoutable)node.newInstance(), BIGRAPH_B0);
				addNodetoParentMap(parent,node, BIGRAPH_B0);

			} else {

				/** TODO add this node to child of parent */
			addNodetoParentMap(parent,node, BIGRAPH_B0);// (Layoutable)node.newInstance(), BIGRAPH_B0);

			}
		}
	}
	
	private void createPushoutPlaceC1() {
		int nSite=0;
		for (Root root : b1.getRoots()) {
			Site site = new Site();
			site.setExtendedData("PROPERTY_NAME", nSite+"");
			nSite++;
			if (interfaceMap.containsKey(root)) {
				/** TODO add site */
				
				addNodetoParentMap(root, site, BIGRAPH_B1);
			} else {
				Layoutable parent = null;
				for (Layoutable child : root.getChildren()) {
					parent = sharedTable.get(child.getName()).getParentB0();
				}
				if (parent != null) {
					/** TODO add site */
					addNodetoParentMap(parent, site, BIGRAPH_B1);
				}
			}
		}
		for (Layoutable node : c1) {
			Layoutable parent = node.getParent();

			if (parent instanceof Root) {

				/** TODO add this to child of interfaceMap.containsKey(root) **/
				addNodetoParentMap(parent, node , BIGRAPH_B1);//(Layoutable)node.newInstance(), BIGRAPH_B1);

			} else {

				/** TODO add this node to child of parent */
				addNodetoParentMap(parent,node, BIGRAPH_B1); //(Layoutable)node.newInstance(), BIGRAPH_B1);

			}
		}
	}
	

	private void createInterfacePlaceList() {
		int nRoot = -1;
		for (SharedObject sobj : v2) {
			if (sobj.isParentRootB0() && sobj.isParentRootB1()) {

				if (interfaceMap.containsKey(sobj.getParentB0())) {
					if (!interfaceMap.containsKey(sobj.getParentB1())) {
						interfaceMap.put((Root) sobj.getParentB1(),
								interfaceMap.get(sobj.getParentB0()));
					} else {
						/** check consistency **/
						if (interfaceMap.get((Root) sobj.getParentB0()) != interfaceMap
								.get((Root) sobj.getParentB1())) {
							System.out
									.println("roots not concides, consistency error");
							return;
						}
					}
				} else {
					if (interfaceMap.containsKey(sobj.getParentB1())) {
						interfaceMap.put((Root) sobj.getParentB0(),
								interfaceMap.get(sobj.getParentB1()));
					} else {
						nRoot = nRoot + 1;
						interfaceMap.put((Root) sobj.getParentB0(), nRoot);
						interfaceMap.put((Root) sobj.getParentB1(), nRoot);
					}
				}
			}
		}
	}

	private void fillUnsharedList() {
		c0 = new ArrayList<Layoutable>();
		c1 = new ArrayList<Layoutable>();
		v2 = new ArrayList<SharedObject>();

		for (SharedObject obj : sharedTable.values()) {
			
			if (!obj.isShared()) {
				if (obj.isNullElement0()) {
					c0.add(obj.getObjectb1());
					System.out.println(obj.getObjectb1().getName() + " ->c0");
				} else if (obj.isNullElement1()) {
					c1.add(obj.getObjectb0());
					System.out.println(obj.getObjectb1().getName() +" ->c1");
				}
			} else {
				v2.add(obj);
				System.out.println(obj.getObjectb1().getName() +" shared");
			}
		}
	}

	private boolean check_consistency() {
		boolean consistent = true;

		for (SharedObject obj : sharedTable.values()) {
			if (obj.isShared()) {
				if (obj.isParentRootB0() || obj.isParentRootB1()) {

				} else {
					SharedObject parentb0 = sharedTable.get(obj.getParentB0()
							.getName());
					SharedObject parentb1 = sharedTable.get(obj.getParentB1()
							.getName());
					if (parentb0.isShared() || parentb1.isShared()) {
						if (!parentb0.equals(parentb1)) {
							return false;
						}
					}
				}

			}

		}
		return consistent;
	}

	private void addBigraph(Bigraph bigraph, String index) {

		for (Layoutable r : bigraph.getChildren()) {// getRoots()) {

			for (Layoutable l : ((Container) r).getChildren()) {
				addElement(l, index);
				if (l instanceof Container) {
					explore(l, index);
				}
			}
		}
	}

	private void explore(Layoutable cont, String index) {
		for (Layoutable l : ((Container) cont).getChildren()) {
			addElement(l, index);
			if (l instanceof Container) {
				explore(l, index);
			}
		}

	}

	public void addElement(Layoutable element, String bigraph) {

		if (bigraph.equals(BIGRAPH_B0)) {
			if (sharedTable.containsKey(element.getName())) {
				sharedTable.get(element.getName()).setElementB0(element);

			} else {
				SharedObject sobj = new SharedObject(element.getName());
				sobj.setElementB0(element);
				sharedTable.put(element.getName(), sobj);
			}
		} else {
			if (sharedTable.containsKey(element.getName())) {
				sharedTable.get(element.getName()).setElementB1(element);

			} else {
				SharedObject sobj = new SharedObject(element.getName());
				sobj.setElementB1(element);
				sharedTable.put(element.getName(), sobj);
			}

		}

	}

	private void reset() {
		sharedTable = new HashMap<String, SharedObject>();
	}

	private void addNodetoParentMap(Layoutable oldparent, Layoutable newNode,
			String bigraph) {
		HashMap<Layoutable, List<Layoutable>> parentMap;
		if (bigraph.equals(BIGRAPH_B0)) {
			parentMap = parentMapBig0;
		} else {
			parentMap = parentMapBig1;
		}
		if (!parentMap.containsKey(oldparent)) {
			parentMap.put(oldparent, new ArrayList<Layoutable>());
		}
		parentMap.get(oldparent).add(newNode);
	}
	
	private void printPushout(){
		System.out.println("Big0");
		for(Entry<Layoutable, List<Layoutable>> entry: parentMapBig0.entrySet()){
			System.out.print("parent: "+entry.getKey().getName() + " ");
			for(Layoutable son: entry.getValue()){
				System.out.print(son.getName() + " ");
			}
			System.out.println(" ");
		}
		System.out.println("Big1");
		for(Entry<Layoutable, List<Layoutable>> entry: parentMapBig1.entrySet()){
			System.out.print("parent: "+entry.getKey().getName() + " ");
			for(Layoutable son: entry.getValue()){
				
				System.out.print(son.getName() + " ");
			}
			System.out.println(" ");
		}
	}

}

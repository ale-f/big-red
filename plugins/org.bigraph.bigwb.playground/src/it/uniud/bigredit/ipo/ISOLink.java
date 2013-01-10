package it.uniud.bigredit.ipo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.Edge;
import org.bigraph.model.InnerName;
import org.bigraph.model.Layoutable;
import org.bigraph.model.Link;
import org.bigraph.model.Point;
import org.bigraph.model.Port;
import org.bigraph.model.Root;
import org.bigraph.model.OuterName;
import org.bigraph.model.Node;
import org.bigraph.model.Site;
import org.bigraph.model.interfaces.BigraphBuilder;
import org.bigraph.model.interfaces.IEdge;
import org.bigraph.model.interfaces.IInnerName;
import org.bigraph.model.interfaces.INode;
import org.bigraph.model.interfaces.IOuterName;
import org.bigraph.model.interfaces.IParent;
import org.bigraph.model.interfaces.IPort;
import org.bigraph.model.interfaces.IRoot;

public class ISOLink {

	private String BIGRAPH_B0 = "bigraph_b0";
	private String BIGRAPH_B1 = "bigraph_b1";

	private HashMap<String, SharedEdge> sharedEdgeMap = new HashMap<String, SharedEdge>();
	private List<Edge> edges0, edges1;
	private List<SharedEdge> edges2;

	private HashMap<String, SharedObject> sharedTable = new HashMap<String, SharedObject>();
	private List<Layoutable> c0, c1;
	private List<SharedObject> v2;
	private HashMap<Root, Integer> interfaceMap = new HashMap<Root, Integer>();
	private HashMap<Layoutable, List<Layoutable>> parentMapBig0 = new HashMap<Layoutable, List<Layoutable>>();
	private HashMap<Layoutable, List<Layoutable>> parentMapBig1 = new HashMap<Layoutable, List<Layoutable>>();

	private HashMap<OuterName, String> interfaceLinkMap = new HashMap<OuterName, String>();

	private Bigraph b0, b1;

	private void addBigraphEdges(Bigraph b, String name) {
		for (Edge e : b.getEdges()) {
			if (!sharedEdgeMap.containsKey(e.getName())) {
				sharedEdgeMap.put(e.getName(), new SharedEdge(e.getName()));
			}
			if (name.equals(BIGRAPH_B0)) {
				sharedEdgeMap.get(name).setEdgeE0(e);
			} else {
				sharedEdgeMap.get(name).setEdgeE1(e);
			}
		}
	}

	public List<Bigraph> analyze(Bigraph b0, Bigraph b1) {
		this.b0 = b0;
		this.b1 = b1;
		addBigraph(b0, BIGRAPH_B0);
		addBigraph(b1, BIGRAPH_B1);
		addBigraphEdges(b0, BIGRAPH_B0);
		addBigraphEdges(b1, BIGRAPH_B1);
		
		boolean consistent = check_consistency();
		if (!consistent) {
			System.out
					.println("the bigraph selected don't respect the consistency properties");
			return null;
		}
		fillUnsharedList();
		fillUnsharedLinkList();

		checkLinkConsistency();
		createInterfacePlaceList();
		createInterfaceLinkList();
		createPushoutPlaceC0();
		createPushoutPlaceC1();
		Bigraph resultC0=createLinksC0();
		Bigraph resultC1=createLinksC1();
		List<Bigraph> result=new ArrayList<Bigraph>();
		result.add(resultC0);
		result.add(resultC1);
		return result;

	}

	/** TODO */
	private boolean checkLinkConsistency() {
		boolean consistent = true;

		for (SharedObject obj : v2) {
			/** check CL0 */
			if (obj.getObjectb0() instanceof Node) {
				Node node0 = (Node) obj.getObjectb0();
				Node node1 = (Node) obj.getObjectb1();
				for (Port port : node0.getPorts()) {
					Link link = port.getLink();
					if (v2.contains(link)) {
						if (!(node1.getPort(port.getName()).getLink()
								.equals(link))) {
							return false;
						}
					} else {
						if (!(node1.getPort(port.getName()).getLink() instanceof OuterName)) {
							return false;
						} else {
							if (!checkLinks(link, node1.getPort(port.getName())
									.getLink())) {
								return false;
							}
						}
					}
				}
				for (Port port : node1.getPorts()) {
					Link link = port.getLink();
					if (v2.contains(link)) {
						if (!(node0.getPort(port.getName()).getLink()
								.equals(link))) {
							return false;
						}
					} else {
						if (!(node0.getPort(port.getName()).getLink() instanceof OuterName)) {
							return false;
						} else {
							if (!checkLinks(node0.getPort(port.getName())
									.getLink(), link)) {
								return false;
							}
						}
					}
				}
			}

		}

		return consistent;
	}

	private boolean checkLinks(Link b0, Link b1) {
		boolean good = true;
		for (Point p : b0.getPoints()) {
			if (sharedTable.get(p.getParent().getName()).isShared()) {
				Layoutable obj1 = sharedTable.get(p.getParent().getName())
						.getObjectb1();
				if (!((Node) obj1).getPort(p.getName()).getLink().equals(b1)) {
					return false;
				}
			} else {
				return false;
			}
		}
		return good;
	}

	private void fillUnsharedLinkList() {
		edges0 = new ArrayList<Edge>();
		edges1 = new ArrayList<Edge>();
		edges2 = new ArrayList<SharedEdge>();

		for (SharedEdge obj : sharedEdgeMap.values()) {

			if (!obj.isShared()) {
				if (obj.isNullEdge0()) {
					edges0.add(obj.getE1());
				} else if (obj.isNullEdge1()) {
					edges1.add(obj.getE0());
				}
			} else {
				edges2.add(obj);
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
					System.out.println(obj.getObjectb1().getName() + " ->c1");
				}
			} else {
				v2.add(obj);
				System.out.println(obj.getObjectb1().getName() + " shared");
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

	private void createInterfaceLinkList() {
		for (SharedObject sobj : v2) {
			if (sobj.isParentRootB0() && sobj.isParentRootB1()) {

				Node node0 = (Node) sobj.getObjectb0();
				Node node1 = (Node) sobj.getObjectb1();

				for (Port port : node0.getPorts()) {
					Link link0 = port.getLink();

					if (link0 instanceof OuterName) {
						Link link1 = node1.getPort(port.getName()).getLink();

						if (link1 instanceof OuterName) {
							if (interfaceLinkMap.containsKey(link0)) {
								if (!interfaceLinkMap.containsKey(link1)) {
									interfaceLinkMap.put((OuterName) link1,
											interfaceLinkMap.get(link0));
								}
							} else {
								if (interfaceLinkMap.containsKey(link1)) {
									interfaceLinkMap.put((OuterName) link0,
											interfaceLinkMap.get(link1));
								} else {
									interfaceLinkMap.put((OuterName) link0,
											link0.getName());
									interfaceLinkMap.put((OuterName) link1,
											link0.getName());

								}
							}
						}
					}

				}
			}
		}
		for (InnerName name0 : b0.getInnerNames()) {
			for (InnerName name1 : b1.getInnerNames()) {
				if (name0.getName().equals(name1.getName())) {
					Link link0 = name0.getLink();
					Link link1 = name1.getLink();

					if (link1 instanceof OuterName) {
						if (interfaceLinkMap.containsKey(link0)) {
							if (!interfaceLinkMap.containsKey(link1)) {
								interfaceLinkMap.put((OuterName) link1,
										interfaceLinkMap.get(link0));
							}
						} else {
							if (interfaceLinkMap.containsKey(link1)) {
								interfaceLinkMap.put((OuterName) link0,
										interfaceLinkMap.get(link1));
							} else {
								interfaceLinkMap.put((OuterName) link0,
										link0.getName());
								interfaceLinkMap.put((OuterName) link1,
										link0.getName());

							}
						}
					}

				}
			}
		}

	}
/************ pushout C0 */
	HashMap<Link, Link> mapOldB0 = new HashMap<Link, Link>();
	HashMap<String, OuterName> newOuterB0 = new HashMap<String, OuterName>();

	private Bigraph createLinksC0() {

		BigraphBuilder bb0 = new BigraphBuilder(b0.getSignature());

		/** create edges */
		for (Edge edge : edges1) {
			IEdge newEdge = bb0.newEdge(edge.getName());
			mapOldB0.put(edge, (Link) newEdge);
		}

		/** createOuterInterface */
		for (OuterName name0 : b0.getOuterNames()) {
			if (interfaceLinkMap.containsKey(name0)) {
				String name = interfaceLinkMap.get(name0);
				if (!newOuterB0.containsKey(name)) {
					IOuterName out = bb0.newOuterName(name);
					newOuterB0.put(name, (OuterName) out);
				}
				mapOldB0.put(name0, newOuterB0.get(name));
			}
		}
		for (OuterName name1 : b1.getOuterNames()) {
			if (interfaceLinkMap.containsKey(name1)) {
				String name = interfaceLinkMap.get(name1);
				if (!newOuterB0.containsKey(name)) {
					IOuterName out = bb0.newOuterName(name);
					newOuterB0.put(name, (OuterName) out);
				}
				mapOldB0.put(name1, newOuterB0.get(name));
			}
		}

		for (OuterName name0 : b0.getOuterNames()) {
			// new OuterName

			IInnerName name = bb0.newInnerName(name0.getName());

			if (interfaceLinkMap.containsKey(name0)) {
				bb0.newConnection(name,
						newOuterB0.get(interfaceLinkMap.get(name0)));
			} else {
				/** get correct edge */
				if (name0.getPoints().size() > 0) {
					Point p = name0.getPoints().get(0);
					SharedObject obj = sharedTable.get(p.getParent().getName());
					Link e = ((Node) obj.getObjectb1()).getPort(p.getName())
							.getLink();
					bb0.newConnection(name, mapOldB0.get(e));
				}
			}
		}

		/** add Nodes and add Links */
		/** explore created parentmap */

		for (Layoutable lay : parentMapBig0.keySet()) {
			if (lay instanceof Root) {
				IRoot root = bb0.newRoot(lay.getName());
				fillContainerC0(root, lay, bb0);
			}
		}
		
		return bb0.finish();
	}

	private void fillContainerC0(IParent container, Layoutable lay,
			BigraphBuilder bb) {
		for (Layoutable son : parentMapBig0.get(lay)) {
			if (son instanceof Site) {
				bb.newSite(container, son.getName());
			} else {
				INode newNode = bb.newNode(container,
						((Node) son).getControl(), son.getName());
				for (IPort port:newNode.getPorts()){
					

					Link link = ((Node) son).getPort(port.getName()).getLink();
					if(link instanceof OuterName){
						bb.newConnection(port, newOuterB0.get(link));
					}else{
						bb.newConnection(port, mapOldB0.get(link));
					}

				}
			}

		}

	}
	
	/********  create PUSHout C1     */
	HashMap<Link, Link> mapOldB1 = new HashMap<Link, Link>();
	HashMap<String, OuterName> newOuterB1 = new HashMap<String, OuterName>();

	private Bigraph createLinksC1() {

		BigraphBuilder bb0 = new BigraphBuilder(b1.getSignature());

		/** create edges */
		for (Edge edge : edges0) {
			IEdge newEdge = bb0.newEdge(edge.getName());
			mapOldB1.put(edge, (Link) newEdge);
		}

		/** createOuterInterface */
		for (OuterName name0 : b0.getOuterNames()) {
			if (interfaceLinkMap.containsKey(name0)) {
				String name = interfaceLinkMap.get(name0);
				if (!newOuterB1.containsKey(name)) {
					IOuterName out = bb0.newOuterName(name);
					newOuterB1.put(name, (OuterName) out);
				}
				mapOldB1.put(name0, newOuterB0.get(name));
			}
		}
		for (OuterName name1 : b1.getOuterNames()) {
			if (interfaceLinkMap.containsKey(name1)) {
				String name = interfaceLinkMap.get(name1);
				if (!newOuterB1.containsKey(name)) {
					IOuterName out = bb0.newOuterName(name);
					newOuterB1.put(name, (OuterName) out);
				}
				mapOldB1.put(name1, newOuterB1.get(name));
			}
		}

		for (OuterName name0 : b1.getOuterNames()) {
			// new OuterName

			IInnerName name = bb0.newInnerName(name0.getName());

			if (interfaceLinkMap.containsKey(name0)) {
				bb0.newConnection(name,
						newOuterB1.get(interfaceLinkMap.get(name0)));
			} else {
				/** get correct edge */
				if (name0.getPoints().size() > 0) {
					Point p = name0.getPoints().get(0);
					SharedObject obj = sharedTable.get(p.getParent().getName());
					Link e = ((Node) obj.getObjectb1()).getPort(p.getName())
							.getLink();
					bb0.newConnection(name, mapOldB1.get(e));
				}
			}
		}

		/** add Nodes and add Links */
		/** explore created parentmap */

		for (Layoutable lay : parentMapBig1.keySet()) {
			if (lay instanceof Root) {
				IRoot root = bb0.newRoot(lay.getName());
				fillContainerC1(root, lay, bb0);
			}
		}
		
		return bb0.finish();
	}

	private void fillContainerC1(IParent container, Layoutable lay,
			BigraphBuilder bb) {
		for (Layoutable son : parentMapBig1.get(lay)) {
			if (son instanceof Site) {
				bb.newSite(container, son.getName());
			} else {
				INode newNode = bb.newNode(container,
						((Node) son).getControl(), son.getName());
				for (IPort port:newNode.getPorts()){
					

					Link link = ((Node) son).getPort(port.getName()).getLink();
					if(link instanceof OuterName){
						bb.newConnection(port, newOuterB1.get(link));
					}else{
						bb.newConnection(port, mapOldB1.get(link));
					}

				}
			}

		}

	}
	
	/*********************************/
	
	
	
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
				// addNodetoParentMap(parent, (Layoutable)node.newInstance(),
				// BIGRAPH_B0);
				addNodetoParentMap(parent, node, BIGRAPH_B0);

			} else {

				/** TODO add this node to child of parent */
				addNodetoParentMap(parent, node, BIGRAPH_B0);// (Layoutable)node.newInstance(),
																// BIGRAPH_B0);

			}
		}
	}

	private void createPushoutPlaceC1() {
		int nSite = 0;
		for (Root root : b1.getRoots()) {
			Site site = new Site();
			site.setExtendedData("PROPERTY_NAME", nSite + "");
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
				addNodetoParentMap(parent, node, BIGRAPH_B1);// (Layoutable)node.newInstance(),
																// BIGRAPH_B1);

			} else {

				/** TODO add this node to child of parent */
				addNodetoParentMap(parent, node, BIGRAPH_B1); // (Layoutable)node.newInstance(),
																// BIGRAPH_B1);

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

}
